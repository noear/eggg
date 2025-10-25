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
 * 类包装器
 *
 * @author noear
 * @since 1.0
 */
public class ClassEggg {
    private final TypeEggg typeEggg;

    private final Object digest;
    private ConstrEggg creator;

    private final List<ConstrEggg> constrEgggs;

    private final Map<String, FieldEggg> fieldEgggsForName = new LinkedHashMap<>();
    private final Map<String, FieldEggg> fieldEgggsForAlias;

    private final List<MethodEggg> methodEgggs;
    private final List<MethodEggg> publicMethodEgggs;
    private final List<MethodEggg> declaredMethodEgggs;

    private final Map<String, PropertyEggg> propertyEgggsForName = new LinkedHashMap<>();
    private final Map<String, PropertyEggg> propertyEgggsForAlias;

    private boolean likeRecordClass = true;
    private boolean realRecordClass;

    private final Eggg eggg;

    public ClassEggg(Eggg eggg, TypeEggg typeEggg) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(typeEggg, "typeEggg");

        this.eggg = eggg;
        this.typeEggg = typeEggg;

        //1.加载字段
        loadFields();

        Method[] declaredMethods = eggg.getDeclaredMethods(typeEggg.getType());
        Method[] methods = eggg.getMethods(typeEggg.getType());

        //2.加载方法
        methodEgggs = new ArrayList<>(declaredMethods.length + methods.length);
        if (methods.length == 0) {
            publicMethodEgggs = Collections.emptyList();
        } else {
            publicMethodEgggs = new ArrayList<>(methods.length);
        }
        if (declaredMethods.length == 0) {
            declaredMethodEgggs = Collections.emptyList();
        } else {
            declaredMethodEgggs = new ArrayList<>(declaredMethods.length);
        }

        loadMethods(declaredMethods, methods);

        //3.加构造器（顺序不能乱）
        Constructor[] declaredConstructors = typeEggg.getType().getDeclaredConstructors();
        constrEgggs = new ArrayList<>(declaredConstructors.length);
        loadConstr(declaredConstructors);

        this.realRecordClass = JavaUtil.isRecordClass(typeEggg.getType());
        this.likeRecordClass = likeRecordClass && fieldEgggsForName.size() > 0;

        fieldEgggsForAlias = new LinkedHashMap<>(fieldEgggsForName.size());
        for (Map.Entry<String, FieldEggg> entry : fieldEgggsForName.entrySet()) {
            fieldEgggsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        propertyEgggsForAlias = new LinkedHashMap<>(propertyEgggsForName.size());
        for (Map.Entry<String, PropertyEggg> entry : propertyEgggsForName.entrySet()) {
            propertyEgggsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        this.digest = eggg.findDigest(this, this, typeEggg.getType(), null);
    }

    /**
     * 真实的记录类
     */
    public boolean isRealRecordClass() {
        return realRecordClass;
    }

    /**
     * 疑似的记录类
     */
    public boolean isLikeRecordClass() {
        return likeRecordClass;
    }

    public TypeEggg getTypeEggg() {
        return typeEggg;
    }

    public Class<?> getType() {
        return typeEggg.getType();
    }

    public Type getGenericType() {
        return typeEggg.getGenericType();
    }

    public Map<String, Type> getGenericInfo() {
        return typeEggg.getGenericInfo();
    }

    /**
     * 获取提炼物
     */
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    /**
     * 获取创造器
     */
    public ConstrEggg getCreator() {
        return creator;
    }

    public ConstrEggg findConstrEggg(Class<?>... parameterTypes) throws NoSuchMethodException {
        ConstrEggg c1 = findConstrEgggOrNull(parameterTypes);

        if (c1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + ".()" + argumentTypesToString(parameterTypes));
        } else {
            return c1;
        }
    }

    public ConstrEggg findConstrEgggOrNull(Class<?>... parameterTypes) throws NoSuchMethodException {
        for (ConstrEggg c1 : constrEgggs) {
            if (c1.getParamCount() == parameterTypes.length) {
                if (parameterTypes.length == 0) {
                    return c1;
                } else {
                    if (Arrays.equals(c1.getConstr().getParameterTypes(), parameterTypes)) {
                        return c1;
                    }
                }
            }
        }

        return null;
    }

    public Collection<MethodEggg> getPublicMethodEgggs() {
        return publicMethodEgggs;
    }

    public Collection<MethodEggg> getDeclaredMethodEgggs() {
        return declaredMethodEgggs;
    }

    public MethodEggg findMethodEggg(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        MethodEggg m1 = findMethodEgggOrNull(name, parameterTypes);

        if (m1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + "." + name + argumentTypesToString(parameterTypes));
        } else {
            return m1;
        }
    }

    public MethodEggg findMethodEgggOrNull(String name, Class<?>... parameterTypes) {
        for (MethodEggg m1 : declaredMethodEgggs) {
            if (m1.getParamCount() == parameterTypes.length && m1.getName().equals(name)) {
                if (parameterTypes.length == 0) {
                    return m1;
                } else {
                    if (Arrays.equals(m1.getMethod().getParameterTypes(), parameterTypes)) {
                        return m1;
                    }
                }
            }
        }

        for (MethodEggg m1 : publicMethodEgggs) {
            if (m1.getParamCount() == parameterTypes.length && m1.getName().equals(name)) {
                if (parameterTypes.length == 0) {
                    return m1;
                } else {
                    if (Arrays.equals(m1.getMethod().getParameterTypes(), parameterTypes)) {
                        return m1;
                    }
                }
            }
        }

        return null;
    }


    public MethodEggg findMethodEgggOrNew(Method method) {
        for (MethodEggg m1 : methodEgggs) {
            if (m1.getMethod().equals(method)) {
                return m1;
            }
        }

        return eggg.newMethodEggg(this, method);
    }

    public List<MethodEggg> getMethodEgggs() {
        return methodEgggs;
    }

    public Collection<FieldEggg> getFieldEgggs() {
        return fieldEgggsForName.values();
    }

    public FieldEggg getFieldEgggByName(String name) {
        return fieldEgggsForName.get(name);
    }

    public FieldEggg getFieldEgggByAlias(String alias) {
        return fieldEgggsForAlias.get(alias);
    }

    public Collection<PropertyEggg> getPropertyEgggs() {
        return propertyEgggsForName.values();
    }

    public PropertyEggg getPropertyEgggByName(String name) {
        return propertyEgggsForName.get(name);
    }

    public PropertyEggg getPropertyEgggByAlias(String alias) {
        return propertyEgggsForAlias.get(alias);
    }

    /// /////////////////

    protected void loadConstr(Constructor[] declaredConstructors) {
        //加载构造器
        for (Constructor c1 : declaredConstructors) {
            constrEgggs.add(new ConstrEggg(eggg, this, c1, eggg.findCreator(c1)));
        }

        //先从静态方法找
        if (typeEggg.getType().isEnum()) {
            for (MethodEggg me : declaredMethodEgggs) {
                if (me.isStatic()) {
                    Annotation constrAnno = eggg.findCreator(me.getMethod());
                    if (constrAnno != null) {
                        creator = eggg.newConstrEggg(this, me.getMethod(), constrAnno);
                        return;
                    }
                }
            }
        }

        //再从构造方法找
        for (ConstrEggg c1 : constrEgggs) {
            if (c1.getConstrAnno() != null) {
                creator = c1;
                return;
            } else if (creator == null) {
                //初始化
                creator = c1;
            } else if (creator.getParamCount() > c1.getParamCount()) {
                //谁参数少，用谁
                creator = c1;
            }
        }
    }

    protected void loadFields() {
        Class<?> clz = typeEggg.getType();

        while (clz != null) {
            for (Field f1 : eggg.getDeclaredFields(clz)) {
                FieldEggg fe = eggg.newFieldEggg(this, f1);

                fieldEgggsForName.put(fe.getName(), fe);

                if (fe.isStatic() == false) {
                    //如果全是只读，则
                    likeRecordClass = likeRecordClass && fe.isFinal();
                    propertyEgggsForName.computeIfAbsent(fe.getName(), k -> new PropertyEggg(k))
                            .setFieldEggg(fe);
                }

            }
            clz = clz.getSuperclass();
        }
    }

    protected void loadMethods(Method[] declaredMethods, Method[] methods) {
        for (Method m1 : declaredMethods) {
            if (m1.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m1.isBridge() == false) {
                MethodEggg methodEggg = eggg.newMethodEggg(this, m1);

                declaredMethodEgggs.add(methodEggg);

                if (methodEggg.isPublic() == false) {
                    //发果是公有，由公有处添加
                    methodEgggs.add(methodEggg);
                }
            }
        }

        for (Method m1 : methods) {
            if (m1.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m1.isBridge()) {
                m1 = findActualMethod(typeEggg.getType().getSuperclass(), m1);
            }

            if (m1 == null) {
                continue;
            }

            MethodEggg me = eggg.newMethodEggg(this, m1);
            publicMethodEgggs.add(me);
            methodEgggs.add(me);

            if (me.isStatic() == false) {
                //属性不能是静态的
                String m1N = m1.getName();
                if (m1N.length() > 2) {
                    if (m1.getReturnType() == void.class && m1.getParameterCount() == 1) {
                        //setter
                        if (m1N.length() > 3 && m1N.startsWith("set")) {
                            PropertyMethodEggg sw = eggg.newPropertyMethodEggg(this, me);

                            propertyEgggsForName.computeIfAbsent(sw.getName(), k -> new PropertyEggg(k))
                                    .setSetterEggg(sw);
                        }
                    } else if (m1.getReturnType() != void.class && m1.getParameterCount() == 0) {
                        //getter
                        if ((m1N.length() > 3 && m1N.startsWith("get")) ||
                                (m1N.length() > 2 && m1N.startsWith("is"))) {
                            PropertyMethodEggg gw = eggg.newPropertyMethodEggg(this, me);

                            propertyEgggsForName.computeIfAbsent(gw.getName(), k -> new PropertyEggg(k))
                                    .setGetterEggg(gw);
                        }
                    }
                }
            }
        }
    }

    private Method findActualMethod(Class<?> clz, Method m1) {
        try {
            m1 = clz.getMethod(m1.getName(), m1.getParameterTypes());

            if (m1 != null) {
                //如果有找到
                if (m1.isBridge()) {
                    //如果还是桥接方法
                    return findActualMethod(clz.getSuperclass(), m1);
                } else {
                    //如果不是桥接方法
                    return m1;
                }
            }
        } catch (NoSuchMethodException ignore) {
        }

        return null;
    }

    @Override
    public String toString() {
        return typeEggg.getType().toString();
    }

    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }
}