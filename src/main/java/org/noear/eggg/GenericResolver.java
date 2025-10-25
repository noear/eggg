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
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Type, Map<String, Type>> genericInfoCached = new ConcurrentHashMap<>();

    /**
     * 清空
     *
     */
    public void clear() {
        genericInfoCached.clear();
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
     * 获取泛型变量和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public Map<String, Type> getGenericInfo(Type type) {
        return genericInfoCached.computeIfAbsent(type, k -> createTypeGenericMap(k));
    }


    /**
     * 创建类中所有的泛型变量和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    private Map<String, Type> createTypeGenericMap(Type type) {
        try {
            final Map<String, Type> typeMap = new HashMap<>();

            // 按继承层级寻找泛型变量和实际类型的对应关系
            // 在类中，对应关系分为两类：
            // 1. 父类定义变量，子类标注实际类型
            // 2. 父类定义变量，子类继承这个变量，让子类的子类去标注，以此类推
            // 此方法中我们将每一层级的对应关系全部加入到Map中，查找实际类型的时候，根据传入的泛型变量，
            // 找到对应关系，如果对应的是继承的泛型变量，则递归继续找，直到找到实际或返回null为止。
            // 如果传入的非Class，例如TypeReference，获取到泛型参数中实际的泛型对象类，继续按照类处理
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
        } catch (Exception ex) { // 优化 5: 将 catch (Throwable ex) 缩小为 catch (Exception ex)
            throw new IllegalStateException("Can't create generic info: " + type, ex);
        }
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
}