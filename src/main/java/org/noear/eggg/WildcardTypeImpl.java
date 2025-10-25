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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * @author noear
 * @since 1.0
 */
public class WildcardTypeImpl implements WildcardType {
    private Type[] upperBounds;
    private Type[] lowerBounds;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds != null ? upperBounds : new Type[0];
        this.lowerBounds = lowerBounds != null ? lowerBounds : new Type[0];

        // 规范化边界
        if (this.upperBounds.length == 0) {
            this.upperBounds = new Type[]{Object.class};
        }

        // 根据 Java 语言规范，上下界不能同时存在
        if (this.lowerBounds.length > 0 &&
                !(this.upperBounds.length > 0 && this.upperBounds[0] == Object.class)) {
            throw new IllegalArgumentException("Wildcard cannot have both lower and upper bounds");
        }
    }

    public static WildcardTypeImpl make(Type[] upperBounds, Type[] lowerBounds) {
        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds.clone(); // 防御性拷贝
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds.clone(); // 防御性拷贝
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WildcardType)) return false;

        WildcardType that = (WildcardType) o;
        return Arrays.equals(getUpperBounds(), that.getUpperBounds()) &&
                Arrays.equals(getLowerBounds(), that.getLowerBounds());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(upperBounds);
        result = 31 * result + Arrays.hashCode(lowerBounds);
        return result;
    }

    @Override
    public String toString() {
        if (lowerBounds.length > 0) {
            return "? super " + boundsToString(lowerBounds);
        } else if (isUnbounded()) {
            return "?";
        } else {
            return "? extends " + boundsToString(upperBounds);
        }
    }

    /**
     * 判断是否为无界通配符
     */
    private boolean isUnbounded() {
        return upperBounds.length == 0 ||
                (upperBounds.length == 1 &&
                        (upperBounds[0] == Object.class ||
                                upperBounds[0].getTypeName().equals("java.lang.Object")));
    }

    /**
     * 将边界数组转换为字符串表示
     */
    private String boundsToString(Type[] bounds) {
        if (bounds.length == 1) {
            return getTypeName(bounds[0]);
        } else {
            // 多个边界用 & 连接
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bounds.length; i++) {
                if (i > 0) {
                    sb.append(" & ");
                }
                sb.append(getTypeName(bounds[i]));
            }
            return sb.toString();
        }
    }

    /**
     * 安全地获取类型名称
     */
    private String getTypeName(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getName();
        } else {
            return type.getTypeName();
        }
    }
}