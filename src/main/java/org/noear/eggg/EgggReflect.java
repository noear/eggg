/*
 * Copyright 2025 ~ noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.eggg;

import java.lang.reflect.*;
import java.util.*;

/**
 * 流式反射调用包装器（jOOR 风格）
 *
 * <p>通过 {@link Eggg#onClass(Class)} 或 {@link Eggg#onBean(Object)} 创建。
 * 内部充分复用 Eggg 体系的元数据缓存和调用能力：
 * <ul>
 *   <li>方法查找：{@link ClassEggg#findMethodEgggOrNull} + 模糊匹配降级</li>
 *   <li>方法调用：{@link MethodEggg#invoke}（含 MethodHandle 加速）</li>
 *   <li>构造器查找：{@link ClassEggg#findConstrEgggOrNull} + 模糊匹配降级</li>
 *   <li>构造器调用：{@link ConstrEggg#newInstance}</li>
 *   <li>字段查找：{@link ClassEggg#getFieldEgggByName}</li>
 *   <li>字段读写：{@link FieldEggg#getValue} / {@link FieldEggg#setValue}</li>
 *   <li>属性查找：{@link ClassEggg#getPropertyEgggByName}</li>
 *   <li>属性读写：{@link PropertyEggg#getValue} / {@link PropertyEggg#setValue}</li>
 * </ul>
 *
 * <pre>{@code
 * Eggg eggg = new Eggg();
 *
 * // 从类开始
 * String result = (String) eggg.onClass(String.class)
 *                         .create("Hello World")
 *                         .call("substring", 6)
 *                         .get();
 *
 * // 从实例开始
 * String result = (String) eggg.onBean("Hello World")
 *                         .call("substring", 6)
 *                         .get();
 *
 * // 字段读写 + 链式
 * eggg.onBean(obj)
 *     .set("name", "Tom")
 *     .call("hello");
 *
 * // 属性读写（走 getter/setter）
 * String name = (String) eggg.onBean(obj).property("name").get();
 * eggg.onBean(obj).setProperty("name", "Tom");
 *
 * // 接口代理
 * MyInterface proxy = eggg.onBean(obj).as(MyInterface.class);
 * proxy.doSomething();
 * }</pre>
 *
 * @author noear
 * @since 1.1
 */
public class EgggReflect {

    // ============ 字段 ============

    private final Eggg eggg;
    private final Class<?> type;
    private final Object object;


    // ============ 构造器（包级，仅由 Eggg 入口创建）============

    /**
     * 包装一个类（用于静态方法调用或创建实例）
     */
    EgggReflect(Eggg eggg, Class<?> type) {
        this.eggg = eggg;
        this.type = type;
        this.object = null;
    }

    /**
     * 包装一个对象实例
     */
    EgggReflect(Eggg eggg, Class<?> type, Object object) {
        this.eggg = eggg;
        this.type = type;
        this.object = object;
    }


    // ============ 流式 API ============


    /**
     * 获取包装的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) object;
    }

    /**
     * 获取包装的类型
     */
    public Class<?> type() {
        return type;
    }


    // ---- create ----

    /**
     * 无参构造
     */
    public EgggReflect create() {
        return create(new Object[0]);
    }

    /**
     * 按参数构造实例（自动匹配构造器）
     *
     * <p>匹配策略（复用 ClassEggg 已有查找能力）：
     * <ol>
     *   <li>精确匹配：{@link ClassEggg#findConstrEgggOrNull}</li>
     *   <li>模糊匹配：基本类型与包装类型互通（降级）</li>
     * </ol>
     *
     * <p>调用方式：{@link ConstrEggg#newInstance}
     */
    public EgggReflect create(Object... args) {
        Class<?>[] argTypes = types(args);

        try {
            ClassEggg classEggg = eggg.getClassEggg(type);

            // 1. 精确匹配（复用 ClassEggg.findConstrEgggOrNull）
            ConstrEggg constrEggg = classEggg.findConstrEgggOrNull(argTypes);

            // 2. 模糊匹配（基本类型 <-> 包装类型，降级）
            if (constrEggg == null) {
                constrEggg = findSimilarConstr(classEggg, argTypes);
            }

            if (constrEggg == null) {
                throw new EgggReflectException(
                    new NoSuchMethodException(
                        "No matching constructor: " + type.getName() + argumentTypesToString(argTypes)));
            }

            // 3. 调用 ConstrEggg.newInstance
            Object instance = constrEggg.newInstance(args);
            return new EgggReflect(eggg, instance.getClass(), instance);

        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }


    // ---- call ----

    /**
     * 调用无参方法
     */
    public EgggReflect call(String name) {
        return call(name, new Object[0]);
    }

    /**
     * 按名称和参数调用方法（自动匹配最佳方法）
     *
     * <p>匹配策略（复用 ClassEggg 已有查找能力）：
     * <ol>
     *   <li>精确匹配：{@link ClassEggg#findMethodEgggOrNull}</li>
     *   <li>模糊匹配：基本类型与包装类型互通（降级）</li>
     * </ol>
     *
     * <p>调用方式：{@link MethodEggg#invoke}（内部自动使用 MethodHandle 加速）
     */
    public EgggReflect call(String name, Object... args) {
        Class<?>[] argTypes = types(args);

        try {
            ClassEggg classEggg = eggg.getClassEggg(type);

            // 1. 精确匹配（复用 ClassEggg.findMethodEgggOrNull）
            MethodEggg methodEggg = classEggg.findMethodEgggOrNull(name, argTypes);

            // 2. 模糊匹配（基本类型 <-> 包装类型，降级）
            if (methodEggg == null) {
                methodEggg = findSimilarMethod(classEggg, name, argTypes);
            }

            if (methodEggg == null) {
                throw new EgggReflectException(
                    new NoSuchMethodException(
                        "No matching method: " + type.getName() + "." + name + argumentTypesToString(argTypes)));
            }

            // 3. 调用 MethodEggg.invoke（内部自动使用 MethodHandle 加速）
            Object result = methodEggg.invoke(object, args);

            // void 方法返回 this，非 void 方法包装结果
            if (methodEggg.getReturnType() == void.class) {
                return this;
            } else if (result == null) {
                return new EgggReflect(eggg, methodEggg.getReturnType(), null);
            } else {
                return new EgggReflect(eggg, result.getClass(), result);
            }

        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }


    // ---- field ----

    /**
     * 获取字段值的包装
     *
     * <p>查找方式：{@link ClassEggg#getFieldEgggByName}
     * <p>取值方式：{@link FieldEggg#getValue}
     */
    public EgggReflect field(String name) {
        try {
            ClassEggg classEggg = eggg.getClassEggg(type);
            FieldEggg fieldEggg = classEggg.getFieldEgggByName(name);

            if (fieldEggg == null) {
                throw new EgggReflectException(
                    new NoSuchFieldException("No field: " + type.getName() + "." + name));
            }

            // FieldEggg.getValue
            Object value = fieldEggg.getValue(object);

            if (value == null) {
                return new EgggReflect(eggg, fieldEggg.getType(), null);
            } else {
                return new EgggReflect(eggg, value.getClass(), value);
            }
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }

    /**
     * 获取字段值（快捷方式，等价于 field(name).get()）
     */
    public <T> T get(String name) {
        return field(name).get();
    }

    /**
     * 设置字段值（返回 this，支持链式调用）
     *
     * <p>查找方式：{@link ClassEggg#getFieldEgggByName}
     * <p>设值方式：{@link FieldEggg#setValue}
     */
    public EgggReflect set(String name, Object value) {
        try {
            ClassEggg classEggg = eggg.getClassEggg(type);
            FieldEggg fieldEggg = classEggg.getFieldEgggByName(name);

            if (fieldEggg == null) {
                throw new EgggReflectException(
                    new NoSuchFieldException("No field: " + type.getName() + "." + name));
            }

            // FieldEggg.setValue
            fieldEggg.setValue(object, value);
            return this;
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }


    // ---- property（走 getter/setter，Eggg 独有优势）----

    /**
     * 获取属性值的包装（优先走 getter）
     *
     * <p>查找方式：{@link ClassEggg#getPropertyEgggByName}
     * <p>取值方式：{@link PropertyEggg#getValue}
     */
    public EgggReflect property(String name) {
        try {
            ClassEggg classEggg = eggg.getClassEggg(type);
            PropertyEggg propEggg = classEggg.getPropertyEgggByName(name);

            if (propEggg == null) {
                return field(name);  // 降级到字段
            }

            // PropertyEggg.getValue(object, true) -- true 表示允许走 getter
            Object value = propEggg.getValue(object, true);

            if (value == null) {
                // 尝试从 getter 或 field 获取类型
                Class<?> propType = Object.class;
                if (propEggg.getGetterEggg() != null) {
                    propType = propEggg.getGetterEggg().getType();
                } else if (propEggg.getFieldEggg() != null) {
                    propType = propEggg.getFieldEggg().getType();
                }
                return new EgggReflect(eggg, propType, null);
            } else {
                return new EgggReflect(eggg, value.getClass(), value);
            }
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }

    /**
     * 设置属性值（优先走 setter，返回 this）
     *
     * <p>查找方式：{@link ClassEggg#getPropertyEgggByName}
     * <p>设值方式：{@link PropertyEggg#setValue}
     */
    public EgggReflect setProperty(String name, Object value) {
        try {
            ClassEggg classEggg = eggg.getClassEggg(type);
            PropertyEggg propEggg = classEggg.getPropertyEgggByName(name);

            if (propEggg == null) {
                return set(name, value);  // 降级到字段
            }

            // PropertyEggg.setValue(object, value, true) -- true 表示允许走 setter
            propEggg.setValue(object, value, true);
            return this;
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }


    // ---- as ----

    /**
     * 将包装对象代理为指定接口类型
     */
    @SuppressWarnings("unchecked")
    public <P> P as(final Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final Object target = object;

        InvocationHandler handler = (proxy, method, args) -> {
            String methodName = method.getName();

            try {
                // 优先按方法名调用（复用 call -> ClassEggg.findMethodEgggOrNull -> MethodEggg.invoke）
                return on(methodName, args).get();
            } catch (EgggReflectException e) {
                if (isMap) {
                    // Map 模式：getter/setter 委托到 Map 操作
                    Map<String, Object> map = (Map<String, Object>) target;
                    int length = (args == null ? 0 : args.length);

                    if (length == 0 && methodName.startsWith("get") && methodName.length() > 3) {
                        String propName = resolvePropertyName(methodName.substring(3));
                        return map.get(propName);
                    } else if (length == 0 && methodName.startsWith("is") && methodName.length() > 2) {
                        String propName = resolvePropertyName(methodName.substring(2));
                        return map.get(propName);
                    } else if (length == 1 && methodName.startsWith("set") && methodName.length() > 3) {
                        String propName = resolvePropertyName(methodName.substring(3));
                        map.put(propName, args[0]);
                        return null;
                    }
                }

                // default 方法支持
                if (method.isDefault()) {
                    return invokeDefaultMethod(proxy, method, args);
                }

                throw e;
            }
        };

        return (P) Proxy.newProxyInstance(
            proxyType.getClassLoader(),
            new Class[]{proxyType},
            handler);
    }


    // ============ 内部方法 ============

    /**
     * 内部快捷调用（给 as() 代理用）
     */
    private EgggReflect on(String name, Object[] args) {
        if (args == null || args.length == 0) {
            return call(name);
        } else {
            return call(name, args);
        }
    }

    /**
     * 将方法名后半部分转为属性名（首字母小写）
     * 例如 "Name" -> "name", "URL" -> "uRL"
     */
    private static String resolvePropertyName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * 模糊匹配方法（基本类型与包装类型互通）
     *
     * <p>仅在 ClassEggg.findMethodEgggOrNull 精确匹配失败后作为降级使用。
     * <p>查找顺序：先声明方法，后公有方法（与 ClassEggg.findMethodEgggOrNull 一致）。
     */
    private MethodEggg findSimilarMethod(ClassEggg classEggg, String name, Class<?>[] argTypes) {
        for (MethodEggg candidate : classEggg.getDeclaredMethodEgggs()) {
            if (candidate.getName().equals(name)
                && matchTypes(candidate.getMethod().getParameterTypes(), argTypes)) {
                return candidate;
            }
        }
        for (MethodEggg candidate : classEggg.getPublicMethodEgggs()) {
            if (candidate.getName().equals(name)
                && matchTypes(candidate.getMethod().getParameterTypes(), argTypes)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 模糊匹配构造器（基本类型与包装类型互通）
     *
     * <p>仅在 ClassEggg.findConstrEgggOrNull 精确匹配失败后作为降级使用。
     */
    private ConstrEggg findSimilarConstr(ClassEggg classEggg, Class<?>[] argTypes) {
        for (ConstrEggg candidate : classEggg.getConstrEgggs()) {
            if (candidate.getConstr() instanceof Constructor) {
                Constructor ctor = (Constructor) candidate.getConstr();
                if (matchTypes(ctor.getParameterTypes(), argTypes)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * 类型匹配（支持基本类型与包装类型互通，支持 null 参数）
     */
    private boolean matchTypes(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length != actualTypes.length) return false;
        for (int i = 0; i < actualTypes.length; i++) {
            if (actualTypes[i] == null) continue;  // null 可匹配任何引用类型
            if (!wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                return false;
            }
        }
        return true;
    }

    /**
     * default 方法调用（JDK 8+）
     *
     * <p>由于项目兼容 JDK 8（source 1.8），不能在编译期直接引用
     * MethodHandles.Lookup 的内部构造器（JDK 9+ 已移除）。因此通过反射间接调用。
     */
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        // 使用反射调用 MethodHandles.Lookup 的私有构造器（JDK 8 可用）
        // 等价于：MethodHandles.Lookup lookup = new MethodHandles.Lookup(method.getDeclaringClass())
        Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        Constructor<?> ctor = lookupClass.getDeclaredConstructor(Class.class);
        ctor.setAccessible(true);
        Object lookup = ctor.newInstance(method.getDeclaringClass());

        // 调用 lookup.unreflectSpecial(method, declaringClass)
        Method unreflectSpecial = lookupClass.getMethod("unreflectSpecial", Method.class, Class.class);
        Object methodHandle = unreflectSpecial.invoke(lookup, method, method.getDeclaringClass());

        // 调用 methodHandle.bindTo(proxy).invokeWithArguments(args)
        Method bindTo = methodHandle.getClass().getMethod("bindTo", Object.class);
        Object boundHandle = bindTo.invoke(methodHandle, proxy);
        Method invokeWithArgs = boundHandle.getClass().getMethod("invokeWithArguments", Object[].class);
        return invokeWithArgs.invoke(boundHandle, new Object[]{args});
    }


    // ============ 静态工具方法 ============

    /**
     * 获取参数对象的类型数组（null 参数用 NULL.class 占位）
     */
    private static Class<?>[] types(Object... values) {
        if (values == null) return new Class[0];
        Class<?>[] result = new Class[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i] == null ? NULL.class : values[i].getClass();
        }
        return result;
    }

    /**
     * 基本类型 -> 包装类型
     */
    @SuppressWarnings("unchecked")
    static <T> Class<T> wrapper(Class<T> type) {
        if (type == null) return null;
        if (type.isPrimitive()) {
            if (boolean.class == type) return (Class<T>) Boolean.class;
            if (int.class == type)     return (Class<T>) Integer.class;
            if (long.class == type)    return (Class<T>) Long.class;
            if (short.class == type)   return (Class<T>) Short.class;
            if (byte.class == type)    return (Class<T>) Byte.class;
            if (double.class == type)  return (Class<T>) Double.class;
            if (float.class == type)   return (Class<T>) Float.class;
            if (char.class == type)    return (Class<T>) Character.class;
            if (void.class == type)    return (Class<T>) Void.class;
        }
        return type;
    }

    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /** null 参数的占位类型 */
    private static class NULL {}

    // ============ Object 方法 ============

    @Override
    public int hashCode() {
        return object == null ? type.hashCode() : object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EgggReflect) {
            Object thatObject = ((EgggReflect) obj).object;
            return object == null ? thatObject == null : object.equals(thatObject);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(object);
    }
}
