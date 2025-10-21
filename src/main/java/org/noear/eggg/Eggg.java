/*
 * Copyright 2005-2025 noear.org and authors
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 泛型蛋
 *
 * @author noear
 * @since 1.0
 */
public class Eggg<EA extends Object> {
    private final Map<Type, TypeWrap> typeWrapLib = new ConcurrentHashMap<>();

    private Class<? extends Annotation> creatorAnnotationClass = null;
    private BiFunction<ClassWrap, AnnotatedElement, EA> attachmentHandler;
    private Function<EA, String> aliasHandler;

    public Eggg<EA> withCreatorAnnotationClass(Class<? extends Annotation> creatorAnnotationClass) {
        this.creatorAnnotationClass = creatorAnnotationClass;
        return this;
    }

    public Eggg<EA> withAttachmentHandler(BiFunction<ClassWrap, AnnotatedElement, EA> attachmentHandler) {
        this.attachmentHandler = attachmentHandler;
        return this;
    }

    public Eggg<EA> withAliasHandler(Function<EA, String> aliasHandler) {
        this.aliasHandler = aliasHandler;
        return this;
    }

    public TypeWrap<EA> newTypeWrap(Type type) {
        return new TypeWrap(this, type);
    }

    public ClassWrap<EA> newClassWrap(TypeWrap typeWrap) {
        return new ClassWrap(this, typeWrap);
    }

    public FieldWrap<EA> newFieldWrap(ClassWrap classWrap, Field field) {
        return new FieldWrap(this, classWrap, field);
    }

    public ConstrWrap<EA> newConstrWrap(ClassWrap classWrap, Executable constr, Annotation constrAnno) {
        return new ConstrWrap(this, classWrap, constr, constrAnno);
    }

    public PropertyMethodWrap<EA> newPropertyMethodWrap(ClassWrap classWrap, Method property) {
        return new PropertyMethodWrap(this, classWrap, property);
    }

    public ParamWrap<EA> newParamWrap(ClassWrap classWrap, Parameter param) {
        return new ParamWrap(this, classWrap, param);
    }


    /// //////////////

    public Annotation findCreator(Executable executable) {
        if (creatorAnnotationClass == null) {
            return null;
        } else {
            return executable.getAnnotation(creatorAnnotationClass);
        }
    }

    public EA findAttachment(ClassWrap classWrap, AnnotatedElement element) {
        if (attachmentHandler == null) {
            return null;
        } else {
            return attachmentHandler.apply(classWrap, element);
        }
    }

    public String findAlias(EA attachment) {
        if (aliasHandler == null) {
            return null;
        } else {
            return aliasHandler.apply(attachment);
        }
    }

    /// //

    public TypeWrap getTypeWrap(Type type) {
        return typeWrapLib.computeIfAbsent(type, t -> newTypeWrap(t));
    }
}