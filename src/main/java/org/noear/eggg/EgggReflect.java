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
 * 流式反射调用包装器
 *
 * <pre>{@code
 * Eggg eggg = new Eggg();
 *
 * // 从类开始
 * String result = eggg.reflect(String.class)
 *                     .create("Hello World")
 *                     .call("substring", 6)
 *                     .get();
 *
 * // 从实例开始
 * String result = eggg.reflect((Object) "Hello World")
 *                     .call("substring", 6)
 *                     .get();
 *
 * // 字段读写 + 链式
 * eggg.reflect(obj)
 *     .setField("name", "Tom")
 *     .call("hello");
 *
 * // 属性读写（走 getter/setter）
 * String name = eggg.reflect(obj).property("name").get();
 * eggg.reflect(obj).setProperty("name", "Tom");
 *
 * // 提取所有属性值为 Map（getter 优先，降级字段）
 * Map<String, Object> map = eggg.reflect(obj).toMap();
 *
 * // 用 Map 批量填充属性（setter 优先，降级字段，未知 key 忽略）
 * eggg.reflect(obj).fillMap(map);
 *
 * }</pre>
 *
 * @author noear
 * @since 1.1
 */
public class EgggReflect {

    // ============ 常量 ============

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    // ============ 字段 ============

    private final Eggg eggg;
    private final TypeEggg typeEggg;
    private final Object object;


    // ============ 构造器（包级，仅由 Eggg 入口创建）============

    /**
     * 包装一个类（用于静态方法调用或创建实例）
     */
    EgggReflect(Eggg eggg, Class<?> type) {
        this(eggg, eggg.getTypeEggg(type), null);
    }

    /**
     * 包装一个对象实例
     */
    EgggReflect(Eggg eggg, Class<?> type, Object object) {
        this(eggg, eggg.getTypeEggg(type), object);
    }

    /**
     * 包装一个类（用于静态方法调用或创建实例）
     */
    EgggReflect(Eggg eggg, TypeEggg typeEggg) {
        this.eggg = eggg;
        this.typeEggg = typeEggg;
        this.object = null;
    }

    /**
     * 包装一个对象实例
     */
    EgggReflect(Eggg eggg, TypeEggg typeEggg, Object object) {
        this.eggg = eggg;
        this.typeEggg = typeEggg;
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
        return typeEggg.getType();
    }


    // ---- create ----

    /**
     * 无参构造
     */
    public EgggReflect create() {
        return create(EMPTY_OBJECT_ARRAY);
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
            ClassEggg classEggg = typeEggg.getClassEggg();

            // 1. 精确匹配（复用 ClassEggg.findConstrEgggOrNull）
            ConstrEggg constrEggg = classEggg.findConstrEgggOrNull(argTypes);

            // 2. 模糊匹配（基本类型 <-> 包装类型，降级）
            if (constrEggg == null) {
                constrEggg = findSimilarConstr(classEggg, argTypes);
            }

            if (constrEggg == null) {
                throw new EgggReflectException(
                    new NoSuchMethodException(
                        "No matching constructor: " + typeEggg.getType().getName() + argumentTypesToString(argTypes)));
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
        return call(name, EMPTY_OBJECT_ARRAY);
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
            ClassEggg classEggg = typeEggg.getClassEggg();

            // 1. 精确匹配（复用 ClassEggg.findMethodEgggOrNull）
            MethodEggg methodEggg = classEggg.findMethodEgggOrNull(name, argTypes);

            // 2. 模糊匹配（基本类型 <-> 包装类型，降级）
            if (methodEggg == null) {
                methodEggg = findSimilarMethod(classEggg, name, argTypes);
            }

            if (methodEggg == null) {
                throw new EgggReflectException(
                    new NoSuchMethodException(
                        "No matching method: " + typeEggg.getType().getName() + "." + name + argumentTypesToString(argTypes)));
            }

            // 3. 实例方法需要 object 不为 null
            if (object == null && !methodEggg.isStatic()) {
                throw new EgggReflectException(
                    new NullPointerException(
                        "Cannot invoke instance method '" + name + "' on null object (type: " + typeEggg.getType().getName() + "). " +
                        "Use create() first or ensure the object is not null."));
            }

            // 4. 调用 MethodEggg.invoke（内部自动使用 MethodHandle 加速）
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


    // ---- field / setField ----

    /**
     * 获取字段值的包装
     *
     * <p>查找方式：{@link ClassEggg#getFieldEgggByName}
     * <p>取值方式：{@link FieldEggg#getValue}
     */
    public EgggReflect field(String name) {
        try {
            ClassEggg classEggg = typeEggg.getClassEggg();
            FieldEggg fieldEggg = classEggg.getFieldEgggByName(name);

            if (fieldEggg == null) {
                throw new EgggReflectException(
                    new NoSuchFieldException("No field: " + typeEggg.getType().getName() + "." + name));
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
     * 设置字段值（返回 this，支持链式调用）
     *
     * <p>查找方式：{@link ClassEggg#getFieldEgggByName}
     * <p>设值方式：{@link FieldEggg#setValue}
     */
    public EgggReflect setField(String name, Object value) {
        try {
            ClassEggg classEggg = typeEggg.getClassEggg();
            FieldEggg fieldEggg = classEggg.getFieldEgggByName(name);

            if (fieldEggg == null) {
                throw new EgggReflectException(
                    new NoSuchFieldException("No field: " + typeEggg.getType().getName() + "." + name));
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
            ClassEggg classEggg = typeEggg.getClassEggg();
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
            ClassEggg classEggg = typeEggg.getClassEggg();
            PropertyEggg propEggg = classEggg.getPropertyEgggByName(name);

            if (propEggg == null) {
                return setField(name, value);  // 降级到字段
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


    // ---- toMap（提取所有属性值为 Map）----

    /**
     * 提取对象的所有属性值为 Map（getter 优先，降级字段）
     *
     * <pre>{@code
     * Map<String, Object> map = eggg.reflect(user).toMap();
     * }</pre>
     *
     * <p>规则：
     * <ul>
     *   <li>遍历所有属性（含父类继承的），key 为属性名（{@link PropertyEggg#getName}）</li>
     *   <li>取值优先走 getter（{@link PropertyEggg#getValue(Object, boolean)}，allowGetter=true），无 getter 时降级读取字段</li>
     *   <li>静态字段不参与（字段加载时已排除）</li>
     *   <li>只写属性（仅有 setter，不可读）会被跳过</li>
     * </ul>
     */
    public Map<String, Object> toMap() {
        return toMap(false);
    }

    /**
     * 提取对象的所有属性值为 Map（getter 优先，降级字段）
     *
     * @param useAlias 是否使用属性别名作为 key（{@link PropertyEggg#getAlias}；需配置 AliasHandler，默认别名与属性名一致）
     */
    public Map<String, Object> toMap(boolean useAlias) {
        try {
            if (object == null) {
                throw new EgggReflectException(
                    new NullPointerException(
                        "Cannot extract properties to map on null object (type: " + typeEggg.getType().getName() + "). " +
                        "Use create() first or ensure the object is not null."));
            }

            ClassEggg classEggg = typeEggg.getClassEggg();
            Map<String, Object> map = new LinkedHashMap<>();

            for (PropertyEggg propEggg : classEggg.getPropertyEgggs()) {
                // 只写属性（仅有 setter，无 getter 也无字段）不可读，跳过
                if (propEggg.getGetterEggg() == null && propEggg.getFieldEggg() == null) {
                    continue;
                }

                // 属性取值（getter 优先，降级字段）
                Object value = propEggg.getValue(object, true);
                map.put(useAlias ? propEggg.getAlias() : propEggg.getName(), value);
            }

            return map;
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }


    // ---- fillMap（用 Map 批量填充属性）----

    /**
     * 用 Map 批量填充对象属性（setter 优先，降级字段），返回 this
     *
     * <pre>{@code
     * eggg.reflect(user).fillMap(map);
     * }</pre>
     *
     * <p>规则（与 {@link #toMap()} 对称）：
     * <ul>
     *   <li>按属性名（{@link PropertyEggg#getName}）匹配 map 的 key，未知 key 忽略</li>
     *   <li>设值优先走 setter（{@link PropertyEggg#setValue(Object, Object, boolean)}，allowSetter=true），无 setter 时降级字段</li>
     *   <li>只读属性（仅有 getter，无 setter 也无字段）跳过</li>
     *   <li>final 字段自动跳过（{@link FieldEggg#setValue(Object, Object)} 内部已处理）</li>
     * </ul>
     */
    public EgggReflect fillMap(Map<String, Object> map) {
        return fillMap(map, false);
    }

    /**
     * 用 Map 批量填充对象属性（setter 优先，降级字段），返回 this
     *
     * @param map     待填充的键值对
     * @param useAlias 是否按属性别名（{@link PropertyEggg#getAlias}）匹配 key（需配置 AliasHandler，默认别名与属性名一致）
     */
    public EgggReflect fillMap(Map<String, Object> map, boolean useAlias) {
        try {
            if (object == null) {
                throw new EgggReflectException(
                    new NullPointerException(
                        "Cannot fill properties from map on null object (type: " + typeEggg.getType().getName() + "). " +
                        "Use create() first or ensure the object is not null."));
            }
            if (map == null) {
                throw new EgggReflectException(
                    new NullPointerException("map must not be null"));
            }

            ClassEggg classEggg = typeEggg.getClassEggg();

            // 建立 key -> 属性 索引（按属性名或别名）
            Map<String, PropertyEggg> propIndex = new HashMap<>();
            for (PropertyEggg propEggg : classEggg.getPropertyEgggs()) {
                propIndex.put(useAlias ? propEggg.getAlias() : propEggg.getName(), propEggg);
            }

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                PropertyEggg propEggg = propIndex.get(entry.getKey());
                if (propEggg == null) {
                    continue;  // 未知 key 忽略
                }

                // 只读属性（仅有 getter，无 setter 也无字段）不可写，跳过
                if (propEggg.getSetterEggg() == null && propEggg.getFieldEggg() == null) {
                    continue;
                }

                // 属性设值（setter 优先，降级字段；final 字段自动跳过）
                propEggg.setValue(object, entry.getValue(), true);
            }

            return this;
        } catch (EgggReflectException e) {
            throw e;
        } catch (Exception e) {
            throw new EgggReflectException(e);
        }
    }

    // ============ 内部方法 ============

    /**
     * 模糊匹配方法（基本类型与包装类型互通）
     *
     * <p>仅在 ClassEggg.findMethodEgggOrNull 精确匹配失败后作为降级使用。
     * <p>使用 {@link ClassEggg#getOwnMethodEgggs()} 合并去重列表，一次遍历完成。
     */
    private MethodEggg findSimilarMethod(ClassEggg classEggg, String name, Class<?>[] argTypes) {
        for (MethodEggg candidate : classEggg.getOwnMethodEgggs()) {
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
            if (matchTypes(candidate.getConstr().getParameterTypes(), argTypes)) {
                return candidate;
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


    // ============ 静态工具方法 ============

    /**
     * 获取参数对象的类型数组（null 参数用 NULL.class 占位）
     */
    private static Class<?>[] types(Object... values) {
        if (values == null || values.length == 0) return EMPTY_CLASS_ARRAY;
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
        return object == null ? typeEggg.hashCode() : object.hashCode();
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