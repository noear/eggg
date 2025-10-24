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

import java.lang.reflect.Parameter;

/**
 * 参数包装器
 *
 * @author noear
 * @since 1.0
 */
public class ParamEggg {
    private final Parameter param;
    private final TypeEggg paramTypeEggg;

    private final String name;
    private final String alias;
    private final Object digest;

    public ParamEggg(Eggg eggg, ClassEggg classEggg, Parameter param) {
        this.param = param;
        this.paramTypeEggg = eggg.getTypeEggg(eggg.reviewType(param.getParameterizedType(), classEggg.getTypeEggg().getGenericInfo()));

        this.name = param.getName();
        this.digest = eggg.findDigest(classEggg, this, param, null);
        this.alias = eggg.findAlias(classEggg, this, digest, name);
    }

    public Parameter getParam() {
        return param;
    }

    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    public TypeEggg getTypeEggg() {
        return paramTypeEggg;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}