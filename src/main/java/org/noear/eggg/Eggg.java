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
    private Class<? extends Annotation> creatorClass = null;

    public Eggg withCreatorClass(Class<? extends Annotation> creatorClass) {
        Objects.requireNonNull(creatorClass, "creatorClass");

        this.creatorClass = creatorClass;
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

    public void clear() {
        typeEgggCached.clear();
        classEgggCached.clear();
    }

    ///

    public TypeEggg getTypeEggg(Type type) {
        Objects.requireNonNull(type, "type");

        if (type instanceof Class<?>) {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isAnonymousClass()) {
                    type = clazz.getGenericSuperclass();
                }
            }
        }

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
        return new TypeEggg(this, type);
    }

    public ClassEggg newClassEggg(TypeEggg typeEggg) {
        return new ClassEggg(this, typeEggg);
    }

    public FieldEggg newFieldEggg(ClassEggg classEggg, Field field) {
        return new FieldEggg(this, classEggg, field);
    }

    public MethodEggg newMethodEggg(ClassEggg classEggg, Method method) {
        return new MethodEggg(this, classEggg, method);
    }

    public ConstrEggg newConstrEggg(ClassEggg classEggg, Executable constr, Annotation constrAnno) {
        return new ConstrEggg(this, classEggg, constr, constrAnno);
    }

    public PropertyMethodEggg newPropertyMethodEggg(ClassEggg classEggg, MethodEggg methodEggg) {
        return new PropertyMethodEggg(this, classEggg, methodEggg);
    }

    public ParamEggg newParamEggg(ClassEggg classEggg, Parameter param) {
        return new ParamEggg(this, classEggg, param);
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
                    Type superType = genericResolver.reviewType(superInte, owner.getGenericInfo());
                    TypeEggg superTypeEggg = getTypeEggg(superType);

                    if (declaringClass.isAssignableFrom(superTypeEggg.getType())) {
                        return findGenericInfo(superTypeEggg, declaringClass);
                    }
                }
            }

            Type superType = genericResolver.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());

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

    protected Annotation findCreator(Executable executable) {
        if (creatorClass == null) {
            return null;
        } else {
            return executable.getAnnotation(creatorClass);
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
     */
    protected Map<String, Type> createGenericInfo(Type type) {
        return genericResolver.createTypeSelfGenericMap(type);
    }

    /**
     * 检查类型
     */
    protected Type reviewType(Type type, Map<String, Type> genericInfo) {
        return genericResolver.reviewType(type, genericInfo);
    }
}