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

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 泛型蛋（泛型包装工具）
 *
 * <pre>{@code
 * // for snack4 demo
 * public class EgggUtil {
 *     private static final Eggg eggg = new Eggg()
 *             .withCreatorClass(ONodeCreator.class)
 *             .withDigestHandler(EgggUtil::doDigestHandle)
 *             .withAliasHandler(EgggUtil::doAliasHandle);
 *
 *     private static String doAliasHandle(ClassEggg cw, AnnotatedEggg s, String ref) {
 *         if (s.getDigest() instanceof ONodeAttrHolder) {
 *             return ((ONodeAttrHolder) s.getDigest()).getAlias();
 *         } else {
 *             return ref;
 *         }
 *     }
 *
 *     private static Object doDigestHandle(ClassEggg cw, AnnotatedEggg s, Object ref) {
 *         ONodeAttr attr = s.getElement().getAnnotation(ONodeAttr.class);
 *
 *         if (attr == null && ref != null) {
 *             return ref;
 *         }
 *
 *         if (s instanceof FieldEggg) {
 *             return new ONodeAttrHolder(attr, ((Field) s.getElement()).getName());
 *         } else if (s instanceof PropertyMethodEggg) {
 *             return new ONodeAttrHolder(attr, Property.resolvePropertyName(((Method) s.getElement()).getName()));
 *         } else if (s instanceof ParamEggg) {
 *             return new ONodeAttrHolder(attr, ((Parameter) s.getElement()).getName());
 *         } else {
 *             return null;
 *         }
 *     }
 *
 *     public static TypeEggg getTypeEggg(Type type) {
 *         return eggg.getTypeEggg(type);
 *     }
 * }
 * }</pre>
 * @author noear
 * @since 1.0
 */
public class Eggg {
    private final Map<Type, SoftReference<TypeEggg>> typeEgggCached = new ConcurrentHashMap<>();
    private final Map<TypeEggg, SoftReference<ClassEggg>> classEgggCached = new ConcurrentHashMap<>();
    private GenericResolver genericResolver = GenericResolver.getDefault();

    private AliasHandler aliasHandler;
    private DigestHandler digestHandler;
    private ReflectHandler reflectHandler = ReflectHandlerDefault.getInstance();
    private CreatorMatcher creatorMatcher;

    public Eggg withCreatorClass(Class<? extends Annotation> creatorClass) {
        Objects.requireNonNull(creatorClass, "creatorClass");

        this.creatorMatcher = (e, s) -> s.isAnnotationPresent(creatorClass);
        return this;
    }

    public Eggg withCreatorMatcher(CreatorMatcher creatorMatcher) {
        Objects.requireNonNull(creatorMatcher, "creatorMatcher");

        this.creatorMatcher = creatorMatcher;
        return this;
    }

    public Eggg withAliasHandler(AliasHandler aliasHandler) {
        Objects.requireNonNull(aliasHandler, "aliasHandler");

        this.aliasHandler = aliasHandler;
        return this;
    }

    public Eggg withDigestHandler(DigestHandler digestHandler) {
        Objects.requireNonNull(digestHandler, "digestHandler");

        this.digestHandler = digestHandler;
        return this;
    }

    public Eggg withReflectHandler(ReflectHandler reflectHandler) {
        Objects.requireNonNull(reflectHandler, "reflectHandler");

        this.reflectHandler = reflectHandler;
        return this;
    }

    public Eggg withGenericResolver(GenericResolver genericResolver) {
        Objects.requireNonNull(genericResolver, "genericResolver");

        this.genericResolver = genericResolver;
        return this;
    }

    ///

    /**
     * 包装一个类，用于创建实例或调用静态方法
     *
     * <pre>{@code
     * String result = (String) eggg.reflect(String.class)
     *                         .create("Hello World")
     *                         .call("substring", 6)
     *                         .get();
     * }</pre>
     */
    public EgggReflect reflect(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return new EgggReflect(this, clazz);
    }

    /**
     * 包装一个已有实例
     *
     * <pre>{@code
     * String result = (String) eggg.reflect("Hello World")
     *                         .call("substring", 6)
     *                         .get();
     * }</pre>
     */
    public EgggReflect reflect(Object object) {
        Objects.requireNonNull(object, "object");
        return new EgggReflect(this, object.getClass(), object);
    }

    ///

    public void clear() {
        typeEgggCached.clear();
        classEgggCached.clear();
    }

    /**
     * 移除指定类型的缓存元数据（用于热插拔场景）
     *
     * <p>会级联移除关联的 ClassEggg 缓存。适用于插件卸载等需要精确清理单个类型的场景。
     *
     * <pre>{@code
     * // 插件卸载时，移除特定类型的元数据
     * eggg.remove(MyPluginClass.class);
     * }</pre>
     *
     * @param type 要移除的类型
     * @return 是否成功移除（true 表示缓存中存在并已移除）
     */
    public boolean remove(Type type) {
        Objects.requireNonNull(type, "type");

        type = resolveCacheType(type);

        SoftReference<TypeEggg> ref = typeEgggCached.remove(type);
        if (ref != null) {
            TypeEggg typeEggg = ref.get();
            if (typeEggg != null) {
                classEgggCached.remove(typeEggg);
            }
            return true;
        }
        return false;
    }

    /**
     * 移除由指定类加载器加载的所有类型的缓存元数据（用于热插拔场景）
     *
     * <p>遍历缓存，移除所有由目标 ClassLoader 加载的类型及其关联的 ClassEggg。
     * 适用于插件系统卸载整个插件时，一次性清理该插件下所有类型的元数据，
     * 避免类加载器泄漏。
     *
     * <pre>{@code
     * // 插件卸载时，按类加载器批量清理
     * eggg.removeByClassLoader(pluginClassLoader);
     * }</pre>
     *
     * @param classLoader 目标类加载器
     * @return 移除的类型数量
     */
    public int removeByClassLoader(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");

        int count = 0;
        Iterator<Map.Entry<Type, SoftReference<TypeEggg>>> it = typeEgggCached.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Type, SoftReference<TypeEggg>> entry = it.next();
            SoftReference<TypeEggg> ref = entry.getValue();
            TypeEggg typeEggg = (ref != null) ? ref.get() : null;

            if (typeEggg != null) {
                Class<?> clazz = typeEggg.getType();
                if (clazz != null && clazz.getClassLoader() == classLoader) {
                    classEgggCached.remove(typeEggg);
                    it.remove();
                    count++;
                }
            } else {
                // SoftReference 已被回收，也清理掉
                it.remove();
            }
        }

        return count;
    }

    /**
     * 移除指定包下所有类型的缓存元数据（用于热插拔场景）
     *
     * <p>遍历缓存，移除所有类名以 {@code packageName} 为前缀的类型及其关联的 ClassEggg。
     * 支持子包匹配，例如传入 "com.example.plugin" 会同时移除
     * "com.example.plugin.service" 和 "com.example.plugin.model" 下的类型。
     *
     * <p>适用于同一 ClassLoader 下按模块/包粒度进行热更新的场景。
     *
     * <pre>{@code
     * // 卸载某个模块的所有类型元数据
     * eggg.removeByPackage("com.example.plugin");
     * }</pre>
     *
     * @param packageName 目标包名（必须为合法包名前缀）
     * @return 移除的类型数量
     */
    public int removeByPackage(String packageName) {
        Objects.requireNonNull(packageName, "packageName");

        // 构建类名前缀："com.example" → "com.example."
        String prefix = packageName.endsWith(".") ? packageName : packageName + ".";

        int count = 0;
        Iterator<Map.Entry<Type, SoftReference<TypeEggg>>> it = typeEgggCached.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Type, SoftReference<TypeEggg>> entry = it.next();
            SoftReference<TypeEggg> ref = entry.getValue();
            TypeEggg typeEggg = (ref != null) ? ref.get() : null;

            if (typeEggg != null) {
                Class<?> clazz = typeEggg.getType();
                if (clazz != null && clazz.getName().startsWith(prefix)) {
                    classEgggCached.remove(typeEggg);
                    it.remove();
                    count++;
                }
            } else {
                // SoftReference 已被回收，也清理掉
                it.remove();
            }
        }

        return count;
    }

    ///

    /**
     * 解析实际缓存类型：匿名子类（如 {@code new HashMap<K,V>() {} }）会被转换为其泛型父类
     */
    private Type resolveCacheType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isAnonymousClass()) {
                return clazz.getGenericSuperclass();
            }
        }
        return type;
    }

    public TypeEggg getTypeEggg(Type type) {
        Objects.requireNonNull(type, "type");

        type = resolveCacheType(type);

        return typeEgggCached.compute(type, (t, softRef) -> {
            if (softRef != null && softRef.get() != null) {
                return softRef;
            }

            return new SoftReference<>(newTypeEggg(t));
        }).get();
    }

    public ClassEggg getClassEggg(TypeEggg typeEggg) {
        Objects.requireNonNull(typeEggg, "typeEggg");

        return classEgggCached.compute(typeEggg, (t, softRef) -> {
            if (softRef != null && softRef.get() != null) {
                return softRef;
            }

            return new SoftReference<>(newClassEggg(t));
        }).get();
    }

    public ClassEggg getClassEggg(Type type) {
        return getTypeEggg(type).getClassEggg();
    }

    ///

    public TypeEggg newTypeEggg(Type type) {
        try {
            return new TypeEggg(this, type);
        } catch (Throwable e) {
            throw new IllegalStateException("The type eggg failed: " + type.getTypeName(), e);
        }
    }

    public ClassEggg newClassEggg(TypeEggg typeEggg) {
        try {
            return new ClassEggg(this, typeEggg);
        } catch (Throwable e) {
            throw new IllegalStateException("The class eggg failed: " + typeEggg.getType().getTypeName(), e);
        }
    }

    public FieldEggg newFieldEggg(ClassEggg classEggg, Field field) {
        return new FieldEggg(this, classEggg, field);
    }

    public MethodEggg newMethodEggg(ClassEggg classEggg, Method method) {
        return new MethodEggg(this, classEggg, method);
    }

    public ConstrEggg newConstrEggg(ClassEggg classEggg, Executable constr, boolean isCreator) {
        return new ConstrEggg(this, classEggg, constr, isCreator);
    }

    public PropertyMethodEggg newPropertyMethodEggg(ClassEggg classEggg, MethodEggg methodEggg) {
        return new PropertyMethodEggg(this, classEggg, methodEggg);
    }

    public ParamEggg newParamEggg(ClassEggg classEggg, ExecutableEggg execEggg, Parameter param) {
        return new ParamEggg(this, classEggg, execEggg, param);
    }

    ///


    /**
     * 查找泛型信息
     */
    public Map<String, Type> findGenericInfo(TypeEggg owner, Class<?> declaringClass) {
        if (declaringClass == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            if (declaringClass.isInterface()) {
                for (Type superInte : owner.getType().getGenericInterfaces()) {
                    Type superType = genericResolver.substituteType(superInte, owner.getGenericInfo());
                    TypeEggg superTypeEggg = getTypeEggg(superType);

                    if (declaringClass.isAssignableFrom(superTypeEggg.getType())) {
                        return findGenericInfo(superTypeEggg, declaringClass);
                    }
                }
            }

            Type superType = genericResolver.substituteType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());

            if (superType == null || superType == Object.class) {
                return owner.getGenericInfo();
            } else {
                return findGenericInfo(getTypeEggg(superType), declaringClass);
            }
        }
    }

    /**
     * 查找泛型信息
     */
    public List<Type> findGenericList(TypeEggg owner, Class<?> declaringClass) {
        Map<String, Type> map = findGenericInfo(owner, declaringClass);

        if (map.isEmpty()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(map.values());
        }
    }

    ///

    protected Object findDigest(ClassEggg classEggg, AnnotatedEggg source, Object defaultValue) {
        if (digestHandler == null) {
            return defaultValue;
        } else {
            return digestHandler.apply(classEggg, source, defaultValue);
        }
    }

    protected String findAlias(ClassEggg classEggg, AnnotatedEggg source, String defaultValue) {
        if (aliasHandler == null) {
            return defaultValue;
        } else {
            return aliasHandler.apply(classEggg, source, defaultValue);
        }
    }

    protected boolean findCreator(Executable executable) {
        if (creatorMatcher == null) {
            return false;
        } else {
            return creatorMatcher.apply(this, executable);
        }
    }

    ///

    /**
     * 获取声明的字段
     */
    protected Field[] getDeclaredFields(Class<?> clazz) {
        return reflectHandler.getDeclaredFields(clazz);
    }

    /**
     * 获取声明的方法
     */
    protected Method[] getDeclaredMethods(Class<?> clazz) {
        return reflectHandler.getDeclaredMethods(clazz);
    }

    /**
     * 获取公有的字段
     */
    protected Method[] getMethods(Class<?> clazz) {
        return reflectHandler.getMethods(clazz);
    }

    ///

    /**
     * 生成泛型信息
     *
     * @param type        原始类型
     * @return 泛型信息 (类型变量名 -> 实际类型)
     */
    protected Map<String, Type> createGenericInfo(Type type) {
        return genericResolver.createTypeSelfGenericMap(type);
    }



    /**
     * 特化类型
     * <p>
     * 将泛型变量替换为具体类型，例如将 {@code List<T>} 特化为 {@code List<String>}
     *
     * @param type        原始类型
     * @param genericInfo 泛型信息 (类型变量名 -> 实际类型)
     * @return 特化后的类型
     */
    protected Type substituteType(Type type, Map<String, Type> genericInfo) {
        return genericResolver.substituteType(type, genericInfo);
    }
}