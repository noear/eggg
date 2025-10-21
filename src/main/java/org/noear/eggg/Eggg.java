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
 * @author noear
 * @since 1.0
 */
public class Eggg implements ReflectHandler {
    private final Map<Type, TypeWrap> typeWrapLib = new ConcurrentHashMap<>();
    private final Map<TypeWrap, ClassWrap> classWrapLib = new ConcurrentHashMap<>();

    private Class<? extends Annotation> creatorAnnotationClass = null;
    private AttachmentHandler attachmentHandler;
    private AliasHandler aliasHandler;
    private ReflectHandler reflectHandler = ReflectHandlerDefault.getInstance();

    public Eggg withCreatorAnnotationClass(Class<? extends Annotation> creatorAnnotationClass) {
        Objects.requireNonNull(creatorAnnotationClass, "creatorAnnotationClass");

        this.creatorAnnotationClass = creatorAnnotationClass;
        return this;
    }

    public <Att extends Object> Eggg withAttachmentHandler(AttachmentHandler<Att> attachmentHandler) {
        Objects.requireNonNull(attachmentHandler, "attachmentHandler");

        this.attachmentHandler = attachmentHandler;
        return this;
    }

    public <Att extends Object> Eggg withAliasHandler(AliasHandler<Att> aliasHandler) {
        Objects.requireNonNull(aliasHandler, "aliasHandler");

        this.aliasHandler = aliasHandler;
        return this;
    }

    public Eggg withReflectHandler(ReflectHandler reflectHandler) {
        Objects.requireNonNull(reflectHandler, "reflectHandler");

        this.reflectHandler = reflectHandler;
        return this;
    }

    ///

    public TypeWrap newTypeWrap(Type type) {
        return new TypeWrap(this, type);
    }

    public ClassWrap newClassWrap(TypeWrap typeWrap) {
        return new ClassWrap(this, typeWrap);
    }

    public FieldWrap newFieldWrap(ClassWrap classWrap, Field field) {
        return new FieldWrap(this, classWrap, field);
    }

    public ConstrWrap newConstrWrap(ClassWrap classWrap, Executable constr, Annotation constrAnno) {
        return new ConstrWrap(this, classWrap, constr, constrAnno);
    }

    public PropertyMethodWrap newPropertyMethodWrap(ClassWrap classWrap, Method property) {
        return new PropertyMethodWrap(this, classWrap, property);
    }

    public ParamWrap newParamWrap(ClassWrap classWrap, Parameter param) {
        return new ParamWrap(this, classWrap, param);
    }

    ///

    public Annotation findCreator(Executable executable) {
        if (creatorAnnotationClass == null) {
            return null;
        } else {
            return executable.getAnnotation(creatorAnnotationClass);
        }
    }

    public Object findAttachment(ClassWrap classWrap, AnnotatedElement element, Object ref) {
        if (attachmentHandler == null) {
            return null;
        } else {
            return attachmentHandler.apply(classWrap, element, ref);
        }
    }

    public String findAlias(Object attachment) {
        if (aliasHandler == null) {
            return null;
        } else {
            return aliasHandler.apply(attachment);
        }
    }

    ///

    public TypeWrap getTypeWrap(Type type) {
        return typeWrapLib.computeIfAbsent(type, t -> newTypeWrap(t));
    }

    public ClassWrap getClassWrap(TypeWrap typeWrap) {
        return classWrapLib.computeIfAbsent(typeWrap, t -> newClassWrap(t));
    }

    ///

    @Override
    public Field[] getDeclaredFields(Class<?> clazz) {
        return reflectHandler.getDeclaredFields(clazz);
    }

    @Override
    public Method[] getDeclaredMethods(Class<?> clazz) {
        return reflectHandler.getDeclaredMethods(clazz);
    }

    @Override
    public Method[] getMethods(Class<?> clazz) {
        return reflectHandler.getMethods(clazz);
    }
}