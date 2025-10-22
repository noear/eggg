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
 * 泛型蛋
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
    private final Map<Type, TypeWrap> typeWrapLib = new ConcurrentHashMap<>();
    private final Map<TypeWrap, ClassWrap> classWrapLib = new ConcurrentHashMap<>();

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


    ///

    public TypeWrap getTypeWrap(Type type) {
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

    public ClassWrap getClassWrap(TypeWrap typeWrap) {
        Objects.requireNonNull(typeWrap, "typeWrap");

        return classWrapLib.computeIfAbsent(typeWrap, t -> newClassWrap(t));
    }


    ///

    protected TypeWrap newTypeWrap(Type type) {
        return new TypeWrap(this, type);
    }

    protected ClassWrap newClassWrap(TypeWrap typeWrap) {
        return new ClassWrap(this, typeWrap);
    }

    protected FieldWrap newFieldWrap(ClassWrap classWrap, Field field) {
        return new FieldWrap(this, classWrap, field);
    }

    protected MethodWrap newMethodWrap(ClassWrap classWrap, Method method) {
        return new MethodWrap(this, classWrap, method);
    }

    protected ConstrWrap newConstrWrap(ClassWrap classWrap, Executable constr, Annotation constrAnno) {
        return new ConstrWrap(this, classWrap, constr, constrAnno);
    }

    protected PropertyMethodWrap newPropertyMethodWrap(ClassWrap classWrap, Method property) {
        return new PropertyMethodWrap(this, classWrap, property);
    }

    protected ParamWrap newParamWrap(ClassWrap classWrap, Parameter param) {
        return new ParamWrap(this, classWrap, param);
    }

    ///

    protected Object findDigest(ClassWrap classWrap, Object holder, AnnotatedElement source, Object ref) {
        if (digestHandler == null) {
            return null;
        } else {
            return digestHandler.apply(classWrap, holder, source, ref);
        }
    }

    protected String findAlias(ClassWrap classWrap, Object holder, Object digest) {
        if (aliasHandler == null) {
            return null;
        } else {
            return aliasHandler.apply(classWrap, holder, digest);
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

    protected Field[] getDeclaredFields(Class<?> clazz) {
        return reflectHandler.getDeclaredFields(clazz);
    }

    protected Method[] getDeclaredMethods(Class<?> clazz) {
        return reflectHandler.getDeclaredMethods(clazz);
    }

    protected Method[] getMethods(Class<?> clazz) {
        return reflectHandler.getMethods(clazz);
    }
}