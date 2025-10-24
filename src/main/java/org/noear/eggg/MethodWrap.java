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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
    private MethodHandle methodHandle;

    private final TypeWrap returnTypeWrap;

    private final Object digest;

    private final Map<String, ParamWrap> paramWrapsForAlias;
    private final List<ParamWrap> paramAry;

    public MethodWrap(Eggg eggg, ClassWrap classWrap, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(classWrap, "classWrap");
        Objects.requireNonNull(method, "property");

        this.method = method;

        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            this.methodHandle = null;
        }

        if (method.getReturnType() != void.class) {
            this.returnTypeWrap = eggg.getTypeWrap(eggg.reviewType(method.getGenericReturnType(), eggg.getMethodGenericInfo(classWrap.getTypeWrap(), method)));
        } else {
            this.returnTypeWrap = null;
        }

        this.digest = eggg.findDigest(classWrap, this, method, null);

        paramWrapsForAlias = new LinkedHashMap<>();
        paramAry = new ArrayList<>();

        for (Parameter p1 : method.getParameters()) {
            ParamWrap paramWrap = eggg.newParamWrap(classWrap, p1);

            paramWrapsForAlias.put(paramWrap.getAlias(), paramWrap);
            paramAry.add(paramWrap);
        }
    }

    public Method getMethod() {
        return method;
    }

    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    public TypeWrap getReturnTypeWrap() {
        return returnTypeWrap;
    }

    /**
     * 只读的
     */
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    /**
     * 静态的
     */
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 公有的
     */
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }

    public String getName() {
        return method.getName();
    }

    public int getParamCount() {
        return paramAry.size();
    }

    public List<ParamWrap> getParamWrapAry() {
        return paramAry;
    }

    public ParamWrap getParamWrapByAlias(String alias) {
        return paramWrapsForAlias.get(alias);
    }

    public boolean hasParamWrapByAlias(String alias) {
        return paramWrapsForAlias.containsKey(alias);
    }

    public <T> T newInstance(Object target, Object... args)
            throws Throwable {

        if (methodHandle == null) {
            if (method.isAccessible() == false) {
                method.setAccessible(true);
            }

            return (T) method.invoke(target, args);
        } else {
            if (target == null && isStatic()) {
                return (T) methodHandle.invokeWithArguments(args);
            } else {
                return (T) methodHandle.bindTo(target).invokeWithArguments(args);
            }
        }
    }

    @Override
    public String toString() {
        return method.toString();
    }
}