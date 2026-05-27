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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.*;
import java.util.*;

/**
 * 方法包装器
 *
 * @author noear
 * @since 1.0
 */
public class MethodEggg implements ExecutableEggg {
    private final Eggg eggg;
    private final ClassEggg ownerEggg;

    private final Method method;
    private MethodHandle methodHandle;

    private final TypeEggg returnTypeEggg;
    private final Map<String, Type> declaredGenericInfo;

    private final Object digest;

    private final Map<String, ParamEggg> paramEgggsForAlias;
    private final List<ParamEggg> paramAry;

    public MethodEggg(Eggg eggg, ClassEggg ownerEggg, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(ownerEggg, "ownerEggg");
        Objects.requireNonNull(method, "method");

        this.eggg = eggg;
        this.ownerEggg = ownerEggg;
        this.method = method;

        try {
            if (isPublic()) {
                this.methodHandle = MethodHandles.lookup().unreflect(method);
            }
        } catch (Throwable e) {
            this.methodHandle = null;
        }

        declaredGenericInfo = eggg.findGenericInfo(ownerEggg.getTypeEggg(), method.getDeclaringClass());

        if (method.getReturnType() != void.class) {
            this.returnTypeEggg = eggg.getTypeEggg(eggg.substituteType(method.getGenericReturnType(), declaredGenericInfo));
        } else {
            this.returnTypeEggg = eggg.getTypeEggg(method.getGenericReturnType());
        }

        this.digest = eggg.findDigest(ownerEggg, this, null);

        if (method.getParameterCount() == 0) {
            paramEgggsForAlias = Collections.emptyMap();
            paramAry = Collections.emptyList();
        } else {
            paramEgggsForAlias = new LinkedHashMap<>(method.getParameterCount());
            paramAry = new ArrayList<>(method.getParameterCount());

            for (Parameter p1 : method.getParameters()) {
                ParamEggg pe = eggg.newParamEggg(ownerEggg, this, p1);

                paramEgggsForAlias.put(pe.getAlias(), pe);
                paramAry.add(pe);
            }
        }
    }

    public ClassEggg getOwnerEggg() {
        return ownerEggg;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public Map<String, Type> getDeclaredGenericInfo() {
        return declaredGenericInfo;
    }

    @Override
    public Type substituteType(Type type) {
        return eggg.substituteType(type, getDeclaredGenericInfo());
    }

    @Override
    public AnnotatedElement getElement() {
        return method;
    }

    @Override
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    public TypeEggg getReturnTypeEggg() {
        return returnTypeEggg;
    }

    public Class<?> getReturnType() {
        return returnTypeEggg.getType();
    }

    public Type getGenericReturnType() {
        return returnTypeEggg.getGenericType();
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

    private Annotation[] annotations;

    @Override
    public Annotation[] getAnnotations() {
        if (annotations == null) {
            annotations = method.getAnnotations();
        }
        return annotations;
    }


    private Parameter[] parameters;

    public Parameter[] getParameters() {
        if (parameters == null) {
            parameters = method.getParameters();
        }
        return parameters;
    }

    public int getParamCount() {
        return method.getParameterCount();
    }

    public Collection<ParamEggg> getParamEgggAry() {
        return paramAry;
    }


    public ParamEggg getParamEgggBy(Parameter p1) {
        for (ParamEggg pe : paramAry) {
            if (p1.equals(pe.getParam())) {
                return pe;
            }
        }

        return null;
    }

    public boolean hasParamEgggBy(Parameter p1) {
        for (ParamEggg pe : paramAry) {
            if (p1.equals(pe.getParam())) {
                return true;
            }
        }

        return false;
    }

    public ParamEggg getParamEgggAt(int index) {
        return paramAry.get(index);
    }

    public ParamEggg getParamEgggByAlias(String alias) {
        return paramEgggsForAlias.get(alias);
    }

    public boolean hasParamEgggByAlias(String alias) {
        return paramEgggsForAlias.containsKey(alias);
    }


    public <T> T invoke(Object target, Object... args)
            throws Throwable {

        if (methodHandle == null) {
            if (method.isAccessible() == false) {
                method.setAccessible(true);
            }

            return (T) method.invoke(target, args);
        } else {
            try {
                if (isStatic()) {
                    // 静态方法：直接传入方法参数
                    return (T) methodHandle.invokeWithArguments(args);
                } else {
                    // 实例方法：需要把 target 作为第一个参数拼接进去
                    Object[] combinedArgs = new Object[args.length + 1];
                    combinedArgs[0] = target;
                    System.arraycopy(args, 0, combinedArgs, 1, args.length);

                    return (T) methodHandle.invokeWithArguments(combinedArgs);
                }
            } catch (WrongMethodTypeException | ClassCastException e) {
                // 场景 A：参数类型不匹配、调用语法错误（属于框架层/调用层不友好异常）
                throw new IllegalArgumentException("Method invocation failed due to argument mismatch for method: " + method.toGenericString(), e);
            } catch (Throwable t) {
                // 场景 B：目标业务方法内部真正抛出的异常
                throw new InvocationTargetException(t, "Target method executed with an exception");
            }
        }
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public String toString() {
        return method.toString();
    }
}