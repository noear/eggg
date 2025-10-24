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
public class ConstrEggg {
    private final Executable constr;

    private final Object digest;

    private final Map<String, ParamEggg> paramAliasMap;
    private final List<ParamEggg> paramAry;

    private final boolean security;

    public ConstrEggg(Eggg eggg, ClassEggg classWrap, Executable constr, Annotation constrAnno) {
        this.constr = constr;

        paramAliasMap = new LinkedHashMap<>();
        paramAry = new ArrayList<>();

        for (Parameter p1 : constr.getParameters()) {
            ParamEggg paramWrap = eggg.newParamWrap(classWrap, p1);

            paramAliasMap.put(paramWrap.getAlias(), paramWrap);
            paramAry.add(paramWrap);
        }

        security = (constr.getParameterCount() == 0 || constrAnno != null || JavaUtil.isRecordClass(classWrap.getTypeWrap().getType()));

        digest = eggg.findDigest(classWrap, this, constr, null);
    }

    public Executable getConstr() {
        return constr;
    }

    public <T extends Object> T getDigest() {
        return (T) digest;
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

    public List<ParamEggg> getParamWrapAry() {
        return paramAry;
    }

    public ParamEggg getParamWrapByAlias(String alias) {
        return paramAliasMap.get(alias);
    }

    public boolean hasParamWrapByAlias(String alias) {
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

    @Override
    public String toString() {
        return constr.toString();
    }
}