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
import java.util.concurrent.locks.ReentrantLock;

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
    // 惰性适配后的调用句柄，统一签名：静态方法 (Object[])Object；实例方法 (Object,Object[])Object。
    // 首次 invoke 时解析一次，之后调用点用 invokeExact 走快路径。
    private volatile MethodHandle methodHandle;
    // 句柄是否已尝试解析（成功或失败都置位）；失败时 methodHandle 保持 null 并回退反射。
    private volatile boolean handleResolved;
    // 只保护句柄解析这一段临界区的专用锁；不锁 this，避免与外部持有本对象引用的代码互相争用。
    private final ReentrantLock handleLock = new ReentrantLock();
    // 形参个数（不含实例方法的 receiver）。
    private final int paramCount;
    private static final Object[] EMPTY_ARGS = new Object[0];

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
        this.paramCount = method.getParameterCount();

        // 构造/扫描期不创建句柄、不 setAccessible，保证扫描零副作用；全部延迟到首次 invoke

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
        return paramCount;
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


    /**
     * 惰性解析并适配调用句柄，只在首次调用时执行一次（双重检查 + 专用锁）。
     * 适配结果：静态方法 (Object[])Object；实例方法 (Object,Object[])Object，供调用点 invokeExact 使用。
     * 任何环节失败（如 JDK9+ 模块限制）都不抛出，句柄保持 null，由 invoke 回退反射。
     */
    private MethodHandle resolveHandle() {
        if (handleResolved) {
            return methodHandle;
        }

        handleLock.lock();
        try {
            if (handleResolved) {
                return methodHandle;
            }

            try {
                if (method.isAccessible() == false) {
                    try {
                        method.setAccessible(true);
                    } catch (SecurityException ignore) {
                        // 不提前返回：public 成员即便 setAccessible 失败，unreflect 通常仍能建句柄；
                        // 若确实建不出，会被下方 catch(Throwable) 兜住并回退反射。
                    }
                }

                MethodHandle mh = MethodHandles.lookup().unreflect(method);
                // varargs 句柄归一为固定元数，否则会把传入的数组当成单个元素再收集一层
                if (mh.isVarargsCollector()) {
                    mh = mh.asFixedArity();
                }

                // generic() 将所有参数（含实例方法的 receiver）泛化为 Object；
                // asSpreader 再把尾部 paramCount 个参数摊平成单个 Object[]。
                // receiver 不计入 paramCount、也不被摊平，故静态/实例两种情形写法一致：
                //   静态 → (Object[])Object，实例 → (Object,Object[])Object。
                mh = mh.asType(mh.type().generic())
                        .asSpreader(Object[].class, paramCount);

                this.methodHandle = mh;
            } catch (Throwable e) {
                this.methodHandle = null;
            } finally {
                this.handleResolved = true;
            }

            return methodHandle;
        } finally {
            handleLock.unlock();
        }
    }

    public <T> T invoke(Object target, Object... args) throws Exception {
        Object[] a = (args == null) ? EMPTY_ARGS : args;

        // 参数个数错误是最常见的开发期失误，校验成本仅一次 int 比较。提前拦截，
        // 使句柄路径与反射回退路径行为一致，两者都抛 IllegalArgumentException。
        if (a.length != paramCount) {
            throw new IllegalArgumentException("Wrong number of arguments: expected " + paramCount
                    + " but got " + a.length + " for method: " + method.toGenericString());
        }

        MethodHandle mh = resolveHandle();

        if (mh == null) {
            // 句柄解析失败时回退反射（setAccessible 已在 resolveHandle 中尝试过）。
            // 反射语义：参数类型不符抛 IllegalArgumentException，方法体异常包 InvocationTargetException。
            return (T) method.invoke(target, a);
        }

        try {
            // (Object) 强转锁定返回类型，使调用点描述符与句柄类型精确匹配，invokeExact 方可生效
            if (isStatic()) {
                return (T) (Object) mh.invokeExact(a);              // (Object[])Object
            } else {
                return (T) (Object) mh.invokeExact(target, a);     // (Object,Object[])Object
            }
        } catch (WrongMethodTypeException e) {
            // 调用点描述符与句柄类型是固定匹配的，走到这里只可能是句柄适配逻辑自身有 bug
            throw new IllegalStateException("Internal handle adaptation error for method: " + method.toGenericString(), e);
        } catch (Throwable t) {
            // invokeExact 无法区分“参数类型转换 CCE/NPE”与“方法体内部业务异常”，
            // 统一按反射语义包成 InvocationTargetException，保证调用方解包习惯不变。
            throw new InvocationTargetException(t, "Target method [" + method.getName() + "] executed with an exception");
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