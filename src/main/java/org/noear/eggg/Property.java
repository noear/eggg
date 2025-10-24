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

/**
 * 属性
 *
 * @author noear
 * @since 1.0
 */
public interface Property {
    static String resolvePropertyName(String methodName) {
        String nameTmp = null;
        if (methodName.startsWith("is")) {
            nameTmp = methodName.substring(2);
        } else {
            nameTmp = methodName.substring(3);
        }

        return nameTmp.substring(0, 1).toLowerCase() + nameTmp.substring(1);
    }

    /**
     * 是临时的（不需要持久化）
     */
    boolean isTransient();

    /**
     * 获取值
     */
    Object getValue(Object target) throws Throwable;

    /**
     * 设置值
     */
    void setValue(Object target, Object value) throws Throwable;

    /**
     * 类型包装
     */
    TypeEggg getTypeEggg();

    /**
     * 名字
     */
    String getName();

    /**
     * 别名
     */
    String getAlias();

    /**
     * 提炼物
     */
    <T extends Object> T getDigest();
}