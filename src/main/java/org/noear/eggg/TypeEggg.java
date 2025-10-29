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
 * 类型包装器
 *
 * @author noear
 * @since 1.0
 */
public class TypeEggg {
    private static final Set<Class<?>> PRIMITIVE_NUMBER_TYPES = new HashSet<>(Arrays.asList(byte.class, int.class, short.class, long.class, float.class, double.class));

    private final Type genericType;
    private final Map<String, Type> genericInfo;

    private Class<?> type = Object.class;

    private final Eggg eggg;

    public TypeEggg(Eggg eggg, Type genericType) {
        this.eggg = eggg;

        if (genericType instanceof Class<?>) {
            this.genericInfo = Collections.unmodifiableMap(eggg.createGenericInfo(genericType));
            this.genericType = genericType;
            this.type = (Class<?>) genericType;
        } else {
            this.genericInfo = Collections.unmodifiableMap(eggg.createGenericInfo(genericType));
            this.genericType = eggg.reviewType(genericType, this.genericInfo);

            if (isParameterizedType()) {
                Type tmp = getParameterizedType().getRawType();

                if (tmp instanceof Class) {
                    type = (Class<?>) tmp;
                }
            } else if (isGenericArrayType()) {
                type = Object[].class;
            } else if (isTypeVariable()) {
                Type tmp = getTypeVariable().getBounds()[0];

                if (tmp instanceof Class) {
                    type = (Class<?>) tmp;
                }
            }
        }
    }

    private ClassEggg classEggg;

    public ClassEggg getClassEggg() {
        if (classEggg == null) {
            classEggg = eggg.getClassEggg(this);
        }

        return classEggg;
    }

    public ClassEggg newClassEggg() {
        return eggg.newClassEggg(this);
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Map<String, Type> getGenericInfo() {
        return genericInfo;
    }

    public boolean isInterface() {
        return type.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(type.getModifiers());
    }

    public boolean isArray() {
        return type.isArray();
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    public boolean isList() {
        return List.class.isAssignableFrom(type);
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(type);
    }

    public boolean isString() {
        return type == String.class;
    }

    public boolean isBoolean() {
        return type == Boolean.class || type == Boolean.TYPE;
    }

    public boolean isNumber() {
        return Number.class.isAssignableFrom(type) || PRIMITIVE_NUMBER_TYPES.contains(type);
    }

    ///

    public boolean isParameterizedType() {
        return genericType instanceof ParameterizedType;
    }

    public ParameterizedType getParameterizedType() {
        return (ParameterizedType) genericType;
    }

    public Type[] getActualTypeArguments() {
        return getParameterizedType().getActualTypeArguments();
    }

    public boolean isGenericArrayType() {
        return genericType instanceof GenericArrayType;
    }

    public GenericArrayType getGenericArrayType() {
        return (GenericArrayType) genericType;
    }

    public boolean isTypeVariable() {
        return genericType instanceof TypeVariable;
    }

    public TypeVariable getTypeVariable() {
        return (TypeVariable) genericType;
    }

    @Override
    public int hashCode() {
        return genericType.hashCode();
    }

    @Override
    public String toString() {
        return genericType.toString();
    }
}