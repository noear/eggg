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

    private Executable constr;
    private Annotation constrAnno;
    private ConstrEggg constrWrap;

    private final Map<String, FieldEggg> fieldWrapsForName = new LinkedHashMap<>();
    private final Map<String, FieldEggg> fieldWrapsForAlias = new LinkedHashMap<>();

    private final List<MethodEggg> publicMethodWraps = new ArrayList<>();
    private final List<MethodEggg> declaredMethodWraps = new ArrayList<>();

    private final Map<String, PropertyEggg> propertyWrapsForName = new LinkedHashMap<>();
    private final Map<String, PropertyEggg> propertyWrapsForAlias = new LinkedHashMap<>();

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
        this.likeRecordClass = likeRecordClass && fieldWrapsForName.size() > 0;

        for (Map.Entry<String, FieldEggg> entry : fieldWrapsForName.entrySet()) {
            fieldWrapsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        for (Map.Entry<String, PropertyEggg> entry : propertyWrapsForName.entrySet()) {
            propertyWrapsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        if (constr != null) {
            constrWrap = eggg.newConstrWrap(this, constr, constrAnno);
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

    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    public ConstrEggg getConstrWrap() {
        return constrWrap;
    }

    public Collection<MethodEggg> getPublicMethodWraps() {
        return publicMethodWraps;
    }

    public Collection<MethodEggg> getDeclaredMethodWraps() {
        return declaredMethodWraps;
    }

    public Collection<FieldEggg> getFieldWraps() {
        return fieldWrapsForName.values();
    }

    public FieldEggg getFieldWrapByName(String name) {
        return fieldWrapsForName.get(name);
    }

    public FieldEggg getFieldWrapByAlias(String alias) {
        return fieldWrapsForAlias.get(alias);
    }

    public Collection<PropertyEggg> getPropertyWraps() {
        return propertyWrapsForName.values();
    }

    public PropertyEggg getPropertyWrapByName(String name) {
        return propertyWrapsForName.get(name);
    }

    public PropertyEggg getPropertyWrapByAlias(String alias) {
        return propertyWrapsForAlias.get(alias);
    }

    /// /////////////////

    protected void loadConstr() {
        if (typeEggg.getType() != Object.class) {
            //先从静态方法找
            for (MethodEggg mw : declaredMethodWraps) {
                if (mw.isStatic()) {
                    constrAnno = eggg.findCreator(mw.getMethod());
                    if (constrAnno != null) {
                        constr = mw.getMethod();
                        break;
                    }
                }
            }

            //再从构造方法找
            if (constr == null) {
                for (Constructor c1 : typeEggg.getType().getDeclaredConstructors()) {
                    if (constr == null) {
                        //初始化
                        constr = c1;
                    } else if (constr.getParameterCount() > c1.getParameterCount()) {
                        //谁参数少，用谁
                        constr = c1;
                    }

                    constrAnno = eggg.findCreator(c1);
                    if (constrAnno != null) {
                        //用注解，优先用
                        constr = c1;
                        break;
                    }
                }
            }
        }
    }

    protected void loadDeclaredFields() {
        Class<?> c = typeEggg.getType();

        while (c != null) {
            for (Field f : eggg.getDeclaredFields(c)) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                FieldEggg fieldWrap = eggg.newFieldWrap(this, f);

                fieldWrapsForName.put(fieldWrap.getName(), fieldWrap);

                if (fieldWrap.isStatic() == false) {
                    //如果全是只读，则
                    likeRecordClass = likeRecordClass && fieldWrap.isFinal();
                    propertyWrapsForName.computeIfAbsent(fieldWrap.getName(), k -> new PropertyEggg(k))
                            .setFieldWrap(fieldWrap);
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
                MethodEggg methodWrap = eggg.newMethodWrap(this, m);
                declaredMethodWraps.add(methodWrap);
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

            MethodEggg methodWrap = eggg.newMethodWrap(this, m);
            publicMethodWraps.add(methodWrap);

            if (methodWrap.isStatic() == false) {
                if (m.getName().length() > 3) {
                    if (m.getReturnType() == void.class && m.getParameterCount() == 1) {
                        //setter
                        if (m.getName().startsWith("set") || m.getName().startsWith("is")) {
                            PropertyMethodEggg sw = eggg.newPropertyMethodWrap(this, m);

                            propertyWrapsForName.computeIfAbsent(sw.getName(), k -> new PropertyEggg(k))
                                    .setSetterWrap(sw);
                        }
                    } else if (m.getReturnType() != void.class && m.getParameterCount() == 0) {
                        //getter
                        if (m.getName().startsWith("get")) {
                            PropertyMethodEggg gw = eggg.newPropertyMethodWrap(this, m);

                            propertyWrapsForName.computeIfAbsent(gw.getName(), k -> new PropertyEggg(k))
                                    .setGetterWrap(gw);
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
}