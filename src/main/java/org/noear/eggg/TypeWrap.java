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
import java.util.Map;

/**
 * 类型包装器
 *
 * @author noear
 * @since 1.0
 */
public class TypeWrap {
    private final Type genericType;
    private final Map<String, Type> genericInfo;

    private Class<?> type = Object.class;

    private boolean isString;
    private boolean isBoolean;
    private boolean isNumber;

    private final Eggg eggg;

    public TypeWrap(Eggg eggg, Type genericType) {
        if (genericType instanceof Class<?>) {
            if (genericType instanceof Class) {
                Class<?> clazz = (Class<?>) genericType;
                if (clazz.isAnonymousClass()) {
                    genericType = clazz.getGenericSuperclass();
                }
            }
        }

        this.eggg = eggg;
        this.genericInfo = GenericUtil.getGenericInfo(genericType);
        this.genericType = GenericUtil.reviewType(genericType, this.genericInfo);

        if (genericType instanceof Class<?>) {
            type = (Class<?>) genericType;

            if (type == String.class) {
                isString = true;
            } else if (type == Boolean.class || type == Boolean.TYPE) {
                isBoolean = true;
            } else if (Number.class.isAssignableFrom(type)) {
                isNumber = true;
            }
        } else if (isParameterizedType()) {
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

    private ClassWrap classWrap;

    public ClassWrap getClassWrap() {
        if (classWrap == null) {
            classWrap = eggg.getClassWrap(this);
        }

        return classWrap;
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

    public boolean isArray() {
        return type.isArray();
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isString() {
        return isString;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public boolean isNumber() {
        return isNumber;
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
    public String toString() {
        return genericType.toString();
    }
}