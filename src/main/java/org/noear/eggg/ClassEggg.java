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

    private final List<ConstrEggg> constrEgggs = new ArrayList<>();

    private final Map<String, FieldEggg> fieldEgggsForName = new LinkedHashMap<>();
    private final Map<String, FieldEggg> fieldEgggsForAlias = new LinkedHashMap<>();

    private final List<MethodEggg> methodEgggs = new ArrayList<>();
    private final List<MethodEggg> publicMethodEgggs = new ArrayList<>();
    private final List<MethodEggg> declaredMethodEgggs = new ArrayList<>();

    private final Map<String, PropertyEggg> propertyEgggsForName = new LinkedHashMap<>();
    private final Map<String, PropertyEggg> propertyEgggsForAlias = new LinkedHashMap<>();

    private boolean likeRecordClass = true;
    private boolean realRecordClass;

    private final Eggg eggg;

    public ClassEggg(Eggg eggg, TypeEggg typeEggg) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(typeEggg, "typeEggg");

        this.eggg = eggg;
        this.typeEggg = typeEggg;

        //顺序不要变
        loadDeclaredFields();
        loadDeclaredMethods();
        loadConstr();

        this.realRecordClass = JavaUtil.isRecordClass(typeEggg.getType());
        this.likeRecordClass = likeRecordClass && fieldEgggsForName.size() > 0;

        for (Map.Entry<String, FieldEggg> entry : fieldEgggsForName.entrySet()) {
            fieldEgggsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

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

    public List<MethodEggg> getMethodEgggs() {
        return methodEgggs;
    }

    public MethodEggg newMethodEggg(Method method) {
        return eggg.newMethodEggg(this, method);
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

    protected void loadConstr() {
        if (typeEggg.getType() != Object.class) {
            //加载构造器
            for (Constructor c1 : typeEggg.getType().getDeclaredConstructors()) {
                constrEgggs.add(new ConstrEggg(eggg, this, c1, eggg.findCreator(c1)));
            }

            //先从静态方法找
            for (MethodEggg mw : declaredMethodEgggs) {
                if (mw.isStatic()) {
                    Annotation constrAnno = eggg.findCreator(mw.getMethod());
                    if (constrAnno != null) {
                        creator = eggg.newConstrEggg(this, mw.getMethod(), constrAnno);
                        return;
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
    }

    protected void loadDeclaredFields() {
        Class<?> c = typeEggg.getType();

        while (c != null) {
            for (Field f : eggg.getDeclaredFields(c)) {
                FieldEggg fieldEggg = eggg.newFieldEggg(this, f);

                fieldEgggsForName.put(fieldEggg.getName(), fieldEggg);

                if (fieldEggg.isStatic() == false) {
                    //如果全是只读，则
                    likeRecordClass = likeRecordClass && fieldEggg.isFinal();
                    propertyEgggsForName.computeIfAbsent(fieldEggg.getName(), k -> new PropertyEggg(k))
                            .setFieldEggg(fieldEggg);
                }

            }
            c = c.getSuperclass();
        }
    }

    protected void loadDeclaredMethods() {
        for (Method m : eggg.getDeclaredMethods(typeEggg.getType())) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m.isBridge() == false) {
                MethodEggg methodEggg = eggg.newMethodEggg(this, m);

                declaredMethodEgggs.add(methodEggg);

                if (methodEggg.isPublic() == false) {
                    //发果是公有，由公有处添加
                    methodEgggs.add(methodEggg);
                }
            }
        }

        for (Method m : eggg.getMethods(typeEggg.getType())) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m.isBridge()) {
                m = findActualMethod(typeEggg.getType().getSuperclass(), m);
            }

            if (m == null) {
                continue;
            }

            MethodEggg methodEggg = eggg.newMethodEggg(this, m);
            publicMethodEgggs.add(methodEggg);
            methodEgggs.add(methodEggg);

            if (methodEggg.isStatic() == false) {
                if (m.getName().length() > 3) {
                    if (m.getReturnType() == void.class && m.getParameterCount() == 1) {
                        //setter
                        if (m.getName().startsWith("set") || m.getName().startsWith("is")) {
                            PropertyMethodEggg sw = eggg.newPropertyMethodEggg(this, m);

                            propertyEgggsForName.computeIfAbsent(sw.getName(), k -> new PropertyEggg(k))
                                    .setSetterEggg(sw);
                        }
                    } else if (m.getReturnType() != void.class && m.getParameterCount() == 0) {
                        //getter
                        if (m.getName().startsWith("get")) {
                            PropertyMethodEggg gw = eggg.newPropertyMethodEggg(this, m);

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