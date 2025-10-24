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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author noear
 * @since 1.0
 * */
public class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> rawType;
    private final Type[] actualTypeArguments;
    private final Type ownerType;

    public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
        this(rawType, actualTypeArguments, null);
    }

    public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
        this.rawType = Objects.requireNonNull(rawType, "Raw type cannot be null");
        this.actualTypeArguments = actualTypeArguments != null ? actualTypeArguments : new Type[0];
        this.ownerType = ownerType;

        // 验证类型参数数量匹配
        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        if (typeParameters.length != this.actualTypeArguments.length) {
            throw new IllegalArgumentException("Type argument count mismatch: " +
                    rawType.getName() + " expects " + typeParameters.length +
                    " but got " + this.actualTypeArguments.length);
        }
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterizedType)) return false;

        ParameterizedType that = (ParameterizedType) o;
        return Objects.equals(rawType, that.getRawType()) &&
                Arrays.equals(actualTypeArguments, that.getActualTypeArguments()) &&
                Objects.equals(ownerType, that.getOwnerType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawType, Arrays.hashCode(actualTypeArguments), ownerType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            sb.append(ownerType.getTypeName()).append(".");
        }

        sb.append(rawType.getTypeName());

        if (actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(actualTypeArguments[i].getTypeName());
            }
            sb.append(">");
        }

        return sb.toString();
    }
}