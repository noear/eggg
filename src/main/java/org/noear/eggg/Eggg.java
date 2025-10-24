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
import java.lang.reflect.*;
import java.util.Map;
import java.util.Objects;
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
 *     private static String doAliasHandle(ClassWrap cw, Object h, Object digest) {
 *         if (digest instanceof ONodeAttrHolder) {
 *             return ((ONodeAttrHolder) digest).getAlias();
 *         } else {
 *             return null;
 *         }
 *     }
 *
 *     private static ONodeAttrHolder doDigestHandle(ClassWrap cw, Object h, AnnotatedElement e, ONodeAttrHolder ref) {
 *         ONodeAttr attr = e.getAnnotation(ONodeAttr.class);
 *
 *         if (attr == null && ref != null) {
 *             return ref;
 *         }
 *
 *         if (h instanceof FieldWrap) {
 *             return new ONodeAttrHolder(attr, ((Field) e).getName());
 *         } else if (h instanceof PropertyMethodWrap) {
 *             return new ONodeAttrHolder(attr, Property.resolvePropertyName(((Method) e).getName()));
 *         } else if (h instanceof ParamWrap) {
 *             return new ONodeAttrHolder(attr, ((Parameter) e).getName());
 *         } else {
 *             return null;
 *         }
 *     }
 *
 *     //获取类型包装器
 *     public static TypeWrap getTypeWrap(Type type) {
 *         return eggg.getTypeWrap(type);
 *     }
 *  }
 * }</pre>
 * @author noear
 * @since 1.0
 */
public class Eggg {
    private final Map<Type, TypeEggg> typeWrapLib = new ConcurrentHashMap<>();
    private final Map<TypeEggg, ClassEggg> classWrapLib = new ConcurrentHashMap<>();
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

    public <T extends Object> Eggg withAliasHandler(AliasHandler<T> aliasHandler) {
        Objects.requireNonNull(aliasHandler, "aliasHandler");

        this.aliasHandler = aliasHandler;
        return this;
    }

    public <T extends Object> Eggg withDigestHandler(DigestHandler<T> digestHandler) {
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
        typeWrapLib.clear();
        classWrapLib.clear();
        genericResolver.clear();
    }

    ///

    public TypeEggg getTypeWrap(Type type) {
        Objects.requireNonNull(type, "type");

        if (type instanceof Class<?>) {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isAnonymousClass()) {
                    type = clazz.getGenericSuperclass();
                }
            }
        }

        return typeWrapLib.computeIfAbsent(type, t -> newTypeWrap(t));
    }

    public ClassEggg getClassWrap(TypeEggg typeWrap) {
        Objects.requireNonNull(typeWrap, "typeWrap");

        return classWrapLib.computeIfAbsent(typeWrap, t -> newClassWrap(t));
    }


    ///

    protected TypeEggg newTypeWrap(Type type) {
        return new TypeEggg(this, type);
    }

    protected ClassEggg newClassWrap(TypeEggg typeWrap) {
        return new ClassEggg(this, typeWrap);
    }

    protected FieldEggg newFieldWrap(ClassEggg classWrap, Field field) {
        return new FieldEggg(this, classWrap, field);
    }

    protected MethodEggg newMethodWrap(ClassEggg classWrap, Method method) {
        return new MethodEggg(this, classWrap, method);
    }

    protected ConstrEggg newConstrWrap(ClassEggg classWrap, Executable constr, Annotation constrAnno) {
        return new ConstrEggg(this, classWrap, constr, constrAnno);
    }

    protected PropertyMethodEggg newPropertyMethodWrap(ClassEggg classWrap, Method property) {
        return new PropertyMethodEggg(this, classWrap, property);
    }

    protected ParamEggg newParamWrap(ClassEggg classWrap, Parameter param) {
        return new ParamEggg(this, classWrap, param);
    }

    ///

    protected Object findDigest(ClassEggg classWrap, Object holder, AnnotatedElement source, Object defaultValue) {
        if (digestHandler == null) {
            return defaultValue;
        } else {
            return digestHandler.apply(classWrap, holder, source, defaultValue);
        }
    }

    protected String findAlias(ClassEggg classWrap, Object holder, Object digest, String defaultValue) {
        if (aliasHandler == null) {
            return defaultValue;
        } else {
            return aliasHandler.apply(classWrap, holder, digest, defaultValue);
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
     * 获取方法的泛型信息
     */
    protected Map<String, Type> getMethodGenericInfo(TypeEggg owner, Method method) {
        if (method.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = genericResolver.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());
            if (superType == null || superType == Object.class) {
                return owner.getGenericInfo();
            } else {
                return getMethodGenericInfo(getTypeWrap(superType), method);
            }
        }
    }

    /**
     * 获取字段的泛型信息
     */
    protected Map<String, Type> getFieldGenericInfo(TypeEggg owner, Field field) {
        if (field.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = genericResolver.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());
            return getFieldGenericInfo(getTypeWrap(superType), field);
        }
    }

    /**
     * 获取泛型信息
     */
    protected Map<String, Type> getGenericInfo(Type type) {
        return genericResolver.getGenericInfo(type);
    }

    /**
     * 检查类型
     */
    protected Type reviewType(Type type, Map<String, Type> genericInfo) {
        return genericResolver.reviewType(type, genericInfo);
    }
}