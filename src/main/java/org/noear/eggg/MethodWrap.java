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
 * 方法包装器
 *
 * @author noear
 * @since 1.0
 */
public class MethodWrap {
    private final Method method;
    private final TypeWrap returnTypeWrap;

    private final Object digest;

    private boolean isFinal;
    private boolean isStatic;
    private boolean isPublic;
    private boolean isDeclared;

    private final Map<String, ParamWrap> paramAliasMap;
    private final List<ParamWrap> paramAry;

    private final Eggg eggg;

    public MethodWrap(Eggg eggg, ClassWrap classWrap, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(classWrap, "classWrap");
        Objects.requireNonNull(method, "property");

        this.eggg = eggg;
        this.method = method;

        if (method.getReturnType() != void.class) {
            this.returnTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(method.getGenericReturnType(), getGenericInfo(classWrap.getTypeWrap(), method)));
        } else {
            this.returnTypeWrap = null;
        }

        this.digest = eggg.findDigest(classWrap, this, method, null);

        this.isFinal = Modifier.isFinal(method.getModifiers());
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.isPublic = Modifier.isPublic(method.getModifiers());
        this.isDeclared = method.getDeclaringClass() == classWrap.getTypeWrap().getType();

        paramAliasMap = new LinkedHashMap<>();
        paramAry = new ArrayList<>();

        for (Parameter p1 : method.getParameters()) {
            ParamWrap paramWrap = eggg.newParamWrap(classWrap, p1);

            paramAliasMap.put(paramWrap.getAlias(), paramWrap);
            paramAry.add(paramWrap);
        }
    }

    public TypeWrap getReturnTypeWrap() {
        return returnTypeWrap;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * 只读的
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * 静态的
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * 公有的
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * 声报的
     *
     */
    public boolean isDeclared() {
        return isDeclared;
    }

    public String getName() {
        return method.getName();
    }

    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    public int getParamCount() {
        return paramAry.size();
    }

    public List<ParamWrap> getParamWrapAry() {
        return paramAry;
    }

    public ParamWrap getParamWrapByAlias(String alias) {
        return paramAliasMap.get(alias);
    }

    public boolean hasParamWrapByAlias(String alias) {
        return paramAliasMap.containsKey(alias);
    }

    public <T> T newInstance(Object target, Object... args)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (method.isAccessible() == false) {
            method.setAccessible(true);
        }

        return (T) method.invoke(target, args);
    }

    @Override
    public String toString() {
        return method.toString();
    }

    private Map<String, Type> getGenericInfo(TypeWrap owner, Method method) {
        if (method.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = GenericUtil.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());
            return getGenericInfo(eggg.getTypeWrap(superType), method);
        }
    }
}