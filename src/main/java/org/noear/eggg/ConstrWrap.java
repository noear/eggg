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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 构造包装器
 *
 * @author noear
 * @since 1.0
 */
public class ConstrWrap<Att extends Object> {
    private final Executable constr;

    private final Map<String, ParamWrap<Att>> paramAliasMap;
    private final List<ParamWrap<Att>> paramAry;

    private final boolean security;

    private final Eggg eggg;

    public ConstrWrap(Eggg eggg, ClassWrap classWrap, Executable constr, Annotation constrAnno) {
        this.eggg = eggg;
        this.constr = constr;

        paramAliasMap = new LinkedHashMap<>();
        paramAry = new ArrayList<>();

        for (Parameter p1 : constr.getParameters()) {
            ParamWrap<Att> paramWrap = eggg.newParamWrap(classWrap, p1);

            paramAliasMap.put(paramWrap.getAlias(), paramWrap);
            paramAry.add(paramWrap);
        }

        security = (constr.getParameterCount() == 0 || constrAnno != null || JavaUtil.isRecordClass(classWrap.getTypeWrap().getType()));
    }

    /**
     * 是否安全（无参数或有注解）
     */
    public boolean isSecurity() {
        return security;
    }

    public int getParamCount() {
        return paramAry.size();
    }

    public List<ParamWrap<Att>> getParamAry() {
        return paramAry;
    }

    public ParamWrap<Att> getParamByAlias(String alias) {
        return paramAliasMap.get(alias);
    }

    public boolean hasParamByAlias(String alias) {
        return paramAliasMap.containsKey(alias);
    }

    public <T> T newInstance(Object... args)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (constr.isAccessible() == false) {
            constr.setAccessible(true);
        }

        if (constr instanceof Constructor) {
            return (T) ((Constructor) constr).newInstance(args);
        } else {
            return (T) ((Method) constr).invoke(args);
        }
    }
}