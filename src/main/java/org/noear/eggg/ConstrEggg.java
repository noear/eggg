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
public class ConstrEggg implements AnnotatedEggg {
    private final ClassEggg ownerEggg;

    private final Executable constr;
    private final boolean isCreator;

    private final Object digest;

    private final Map<String, ParamEggg> paramAliasMap;
    private final List<ParamEggg> paramAry;

    private final boolean security;

    public ConstrEggg(Eggg eggg, ClassEggg ownerEggg, Executable constr, boolean isCreator) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(ownerEggg, "ownerEggg");
        Objects.requireNonNull(constr, "constr");

        this.ownerEggg = ownerEggg;
        this.constr = constr;
        this.isCreator = isCreator;

        if (constr.getParameterCount() == 0) {
            paramAliasMap = Collections.emptyMap();
            paramAry = Collections.emptyList();
        } else {
            paramAliasMap = new LinkedHashMap<>(constr.getParameterCount());
            paramAry = new ArrayList<>(constr.getParameterCount());

            for (Parameter p1 : constr.getParameters()) {
                ParamEggg pe = eggg.newParamEggg(ownerEggg, p1);

                paramAliasMap.put(pe.getAlias(), pe);
                paramAry.add(pe);
            }
        }

        security = (constr.getParameterCount() == 0 || isCreator || ownerEggg.isRealRecordClass());

        digest = eggg.findDigest(ownerEggg, this, null);
    }

    public ClassEggg getOwnerEggg() {
        return ownerEggg;
    }

    public Executable getConstr() {
        return constr;
    }

    protected boolean isCreator() {
        return isCreator;
    }

    @Override
    public AnnotatedElement getElement() {
        return constr;
    }

    @Override
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    private Annotation[] annotations;

    @Override
    public Annotation[] getAnnotations() {
        if (annotations == null) {
            annotations = constr.getAnnotations();
        }
        return annotations;
    }

    /**
     * 是否安全（无参数或有注解）
     */
    public boolean isSecurity() {
        return security;
    }

    public int getParamCount() {
        return constr.getParameterCount();
    }

    public List<ParamEggg> getParamEgggAry() {
        return paramAry;
    }

    public ParamEggg getParamEgggByAlias(String alias) {
        return paramAliasMap.get(alias);
    }

    public boolean hasParamEgggByAlias(String alias) {
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