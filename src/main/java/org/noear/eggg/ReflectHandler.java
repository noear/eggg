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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射处理器（预留原生编译扩展）
 *
 * @author noear
 * @since 1.0
 */
public interface ReflectHandler {
    /**
     * 获取声明的字段
     */
    Field[] getDeclaredFields(Class<?> clazz);

    /**
     * 获取声明的方法
     */
    Method[] getDeclaredMethods(Class<?> clazz);

    /**
     * 获取公有的方法
     */
    Method[] getMethods(Class<?> clazz);
}