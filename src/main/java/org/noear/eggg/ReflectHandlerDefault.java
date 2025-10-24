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
import java.util.Objects;

/**
 *
 * @author noear
 * @since 1.0
 */
public class ReflectHandlerDefault implements ReflectHandler {
    private static ReflectHandler instance = new ReflectHandlerDefault();

    public static ReflectHandler getInstance() {
        return instance;
    }

    public static void setInstance(ReflectHandler instance) {
        Objects.requireNonNull(instance, "instance");

        ReflectHandlerDefault.instance = instance;
    }

    @Override
    public Field[] getDeclaredFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    @Override
    public Method[] getDeclaredMethods(Class<?> clazz) {
        return clazz.getDeclaredMethods();
    }

    @Override
    public Method[] getMethods(Class<?> clazz) {
        return clazz.getMethods();
    }
}