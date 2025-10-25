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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author noear
 * @since 1.0
 */
public class GenericArrayTypeImpl implements GenericArrayType {
    private final Type genericComponentType;

    public GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = Objects.requireNonNull(genericComponentType, "genericComponentType");
    }

    @Override
    public Type getGenericComponentType() {
        return genericComponentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericArrayType)) return false;

        GenericArrayType that = (GenericArrayType) o;
        return Objects.equals(genericComponentType, that.getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(genericComponentType);
    }

    @Override
    public String toString() {
        return genericComponentType.getTypeName() + "[]";
    }
}