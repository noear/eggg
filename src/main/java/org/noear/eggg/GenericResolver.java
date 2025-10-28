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
 * 泛型分析器
 *
 * @author noear
 * @since 1.0
 */
public class GenericResolver {
    private static GenericResolver _default = new GenericResolver();

    public static GenericResolver getDefault() {
        return _default;
    }

    /**
     * 创建类所有的泛型变量和泛型实际类型的对应关系Map（可能会失真）
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public Map<String, Type> createTypeDeepGenericMap(Type type) {
        try {
            final Map<String, Type> typeMap = new HashMap<>();

            while (null != type) {
                final ParameterizedType parameterizedType = toParameterizedType(type, typeMap);
                if (null == parameterizedType) {
                    break;
                }
                final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                final TypeVariable[] typeParameters = rawType.getTypeParameters();

                for (int i = 0; i < typeParameters.length; i++) {
                    typeMap.putIfAbsent(typeParameters[i].getTypeName(), typeArguments[i]);
                }

                type = rawType;
            }

            return typeMap;
        } catch (Exception ex) {
            throw new IllegalStateException("Can't create generic info: " + type, ex);
        }
    }

    /**
     * 创建类本级的泛型变量和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public Map<String, Type> createTypeSelfGenericMap(Type type) {
        try {
            final Map<String, Type> typeMap = new HashMap<>();

            while (null != type) {
                final ParameterizedType parameterizedType = toParameterizedType(type, typeMap);
                if (null == parameterizedType) {
                    break;
                }
                final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                final TypeVariable[] typeParameters = rawType.getTypeParameters();

                for (int i = 0; i < typeParameters.length; i++) {
                    typeMap.putIfAbsent(typeParameters[i].getTypeName(), typeArguments[i]);
                }

                type = rawType;
            }

            if (typeMap.isEmpty()) {
                return Collections.emptyMap();
            } else {
                return typeMap;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Can't create generic info: " + type, ex);
        }
    }

    /**
     * 转换为参数化类型
     *
     * @param genericInfo 泛型信息
     */
    public ParameterizedType toParameterizedType(Type type, Map<String, Type> genericInfo) {
        if (type == null) {
            return null;
        }

        ParameterizedType result = null;
        if (type instanceof ParameterizedType) {
            result = (ParameterizedType) type;

            if (genericInfo != null && genericInfo.size() > 0) {
                //如果有泛型信息，做二次分析转换变量符
                boolean typeArgsChanged = false;
                Type[] typeArgs = result.getActualTypeArguments();
                Class<?> rawClz = (Class<?>) result.getRawType();
                for (int i = 0; i < typeArgs.length; i++) {
                    Type typeArg1 = typeArgs[i];
                    if (typeArg1 instanceof TypeVariable) {
                        typeArg1 = genericInfo.get(typeArg1.getTypeName());
                        if (typeArg1 != null) {
                            typeArgsChanged = true;
                            typeArgs[i] = typeArg1;
                        }
                    }
                }

                if (typeArgsChanged) {
                    result = new ParameterizedTypeImpl(rawClz, typeArgs, result.getOwnerType());
                }
            }
        } else if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            Type genericSuper = clazz.getGenericSuperclass();
            if (null == genericSuper || Object.class.equals(genericSuper)) {
                // 如果类没有父类，而是实现一些定义好的泛型接口，则取接口的 Type
                final Type[] genericInterfaces = clazz.getGenericInterfaces();
                if (genericInterfaces != null && genericInterfaces.length > 0) {
                    // 默认取第一个实现接口的泛型 Type
                    genericSuper = genericInterfaces[0];
                }
            }

            result = toParameterizedType(genericSuper, genericInfo);
        }
        return result;
    }

    /**
     * 审查类型
     *
     * @param type        原始类型
     * @param genericInfo 泛型信息
     * @since 3.0
     *
     */
    public Type reviewType(Type type, Map<String, Type> genericInfo) {
        return reviewType(type, genericInfo, new HashSet<>());
    }

    /**
     * 审查类型
     *
     * @param type        原始类型
     * @param genericInfo 泛型信息
     * @param visited     已访问的类型集合，防止循环引用
     */
    private Type reviewType(Type type, Map<String, Type> genericInfo, Set<Type> visited) {
        if (genericInfo == null || genericInfo.isEmpty() || type instanceof Class) {
            return type;
        }

        // 防止循环引用
        if (!visited.add(type)) {
            return type;
        }

        try {
            if (type instanceof TypeVariable) {
                Type resolved = genericInfo.get(type.getTypeName());
                return resolved != null ? reviewType(resolved, genericInfo, visited) : type;

            } else if (type instanceof WildcardType) {
                return reviewWildcardType((WildcardType) type, genericInfo, visited);

            } else if (type instanceof ParameterizedType) {
                return reviewParameterizedType((ParameterizedType) type, genericInfo, visited);

            } else if (type instanceof GenericArrayType) {
                return reviewGenericArrayType((GenericArrayType) type, genericInfo, visited);
            }

            return type;
        } finally {
            visited.remove(type);
        }
    }

    private Type reviewWildcardType(WildcardType wildcardType, Map<String, Type> genericInfo, Set<Type> visited) {
        Type[] upperBounds = reviewTypes(wildcardType.getUpperBounds(), genericInfo, visited);
        Type[] lowerBounds = reviewTypes(wildcardType.getLowerBounds(), genericInfo, visited);

        // 如果边界没有变化，返回原类型
        if (Arrays.equals(upperBounds, wildcardType.getUpperBounds()) &&
                Arrays.equals(lowerBounds, wildcardType.getLowerBounds())) {
            return wildcardType;
        }

        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    private Type reviewParameterizedType(ParameterizedType parameterizedType, Map<String, Type> genericInfo, Set<Type> visited) {
        Type[] typeArgs = reviewTypes(parameterizedType.getActualTypeArguments(), genericInfo, visited);

        // 如果类型参数没有变化，返回原类型
        if (Arrays.equals(typeArgs, parameterizedType.getActualTypeArguments())) {
            return parameterizedType;
        }

        return new ParameterizedTypeImpl(
                (Class<?>) parameterizedType.getRawType(),
                typeArgs,
                parameterizedType.getOwnerType()
        );
    }

    private Type reviewGenericArrayType(GenericArrayType genericArrayType, Map<String, Type> genericInfo, Set<Type> visited) {
        Type componentType = reviewType(genericArrayType.getGenericComponentType(), genericInfo, visited);

        if (componentType == genericArrayType.getGenericComponentType()) {
            return genericArrayType;
        }

        return new GenericArrayTypeImpl(componentType);
    }

    private Type[] reviewTypes(Type[] types, Map<String, Type> genericInfo, Set<Type> visited) {
        Type[] result = new Type[types.length];
        boolean changed = false;

        for (int i = 0; i < types.length; i++) {
            result[i] = reviewType(types[i], genericInfo, visited);
            if (result[i] != types[i]) {
                changed = true;
            }
        }

        return changed ? result : types;
    }


    /**
     * @author noear
     * @since 1.0
     */
    public static class GenericArrayTypeImpl implements GenericArrayType {
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

    public static class WildcardTypeImpl implements WildcardType {
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

    public static class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] actualTypeArguments;
        private final Type ownerType;

        public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
            this(rawType, actualTypeArguments, null);
        }

        public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
            this.rawType = Objects.requireNonNull(rawType, "rawType");
            this.actualTypeArguments = actualTypeArguments != null ? actualTypeArguments : new Type[0];
            this.ownerType = ownerType;

            // 验证类型参数数量匹配
            TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
            if (typeParameters.length != this.actualTypeArguments.length) {
                throw new IllegalArgumentException("Argument length mismatch");
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
            int result = Objects.hashCode(rawType);
            result = 31 * result + Arrays.hashCode(actualTypeArguments);
            result = 31 * result + Objects.hashCode(ownerType);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (ownerType != null) {
                sb.append(ownerType.getTypeName()).append("$");
            }

            sb.append(rawType.getTypeName());

            if (actualTypeArguments.length > 0) {
                sb.append("<");
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (i > 0) sb.append(", ");
                    Type arg = actualTypeArguments[i];
                    sb.append(arg.getTypeName());
                }
                sb.append(">");
            }

            return sb.toString();
        }
    }
}