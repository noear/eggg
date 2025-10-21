/*
 * Copyright 2005-2025 noear.org and authors
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
public class ParamWrap<EA extends Object> {
    private final Parameter param;
    private final TypeWrap paramTypeWrap;

    private final String name;
    private final String alias;
    private final EA attachment;

    public ParamWrap(Eggg eggg, ClassWrap classWrap, Parameter param) {
        this.param = param;
        this.paramTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(param.getParameterizedType(), classWrap.getTypeWrap().getGenericInfo()));

        this.name = param.getName();
        this.attachment = (EA) eggg.findAttachment(classWrap, param, null);
        this.alias = eggg.findAlias(attachment);
    }

    public Parameter getParam() {
        return param;
    }

    public TypeWrap getTypeWrap() {
        return paramTypeWrap;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public EA getAttachment() {
        return attachment;
    }
}