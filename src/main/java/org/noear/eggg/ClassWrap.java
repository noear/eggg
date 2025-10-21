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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 类包装器
 *
 * @author noear
 * @since 1.0
 */
public class ClassWrap<EA extends Object> {
    private final TypeWrap typeWrap;

    private Executable constr;
    private Annotation constrAnno;
    private ConstrWrap constrWrap;

    private final Map<String, FieldWrap<EA>> fieldWrapsForName = new LinkedHashMap<>();
    private final Map<String, PropertyWrap<EA>> propertyWrapsForName = new LinkedHashMap<>();
    private final Map<String, PropertyWrap<EA>> propertyWrapsForAlias = new LinkedHashMap<>();

    private boolean likeRecordClass = true;
    private boolean realRecordClass;

    private final Eggg eggg;

    public ClassWrap(Eggg eggg, TypeWrap typeWrap) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(typeWrap, "typeWrap");

        this.eggg = eggg;
        this.typeWrap = typeWrap;

        loadConstr();
        loadDeclaredFields();
        loadDeclaredPropertys();

        this.realRecordClass = JavaUtil.isRecordClass(typeWrap.getType());
        this.likeRecordClass = likeRecordClass && fieldWrapsForName.size() > 0;

        for (Map.Entry<String, PropertyWrap<EA>> entry : propertyWrapsForName.entrySet()) {
            propertyWrapsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        if (constr != null) {
            constrWrap = eggg.newConstrWrap(this, constr, constrAnno);
        }
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

    public TypeWrap getTypeWrap() {
        return typeWrap;
    }

    public ConstrWrap getConstrWrap() {
        return constrWrap;
    }

    public Map<String, FieldWrap<EA>> getFieldWrapsForName() {
        return fieldWrapsForName;
    }

    public FieldWrap<EA> getFieldWrapByName(String name) {
        return fieldWrapsForName.get(name);
    }

    public Map<String, PropertyWrap<EA>> getPropertyWrapsForName() {
        return propertyWrapsForName;
    }

    public Map<String, PropertyWrap<EA>> getPropertyWrapsForAlias() {
        return propertyWrapsForAlias;
    }

    public PropertyWrap<EA> getPropertyWrapByAlias(String alias) {
        return propertyWrapsForAlias.get(alias);
    }

    public PropertyWrap<EA> getPropertyWrapByName(String name) {
        return propertyWrapsForName.get(name);
    }

    /// /////////////////

    protected void loadConstr() {
        if (typeWrap.getType() != Object.class) {
            //先从静态方法找
            for (Method m1 : typeWrap.getType().getDeclaredMethods()) {
                if (Modifier.isStatic(m1.getModifiers())) {
                    constrAnno = eggg.findCreator(m1);
                    if (constrAnno != null) {
                        constr = m1;
                        break;
                    }
                }
            }

            //再从构造方法找
            if (constr == null) {
                for (Constructor c1 : typeWrap.getType().getDeclaredConstructors()) {
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
        Class<?> c = typeWrap.getType();

        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                FieldWrap fieldWrap = eggg.newFieldWrap(this, f);

                //如果全是只读，则
                likeRecordClass = likeRecordClass && fieldWrap.isFinal();

                fieldWrapsForName.put(fieldWrap.getName(), fieldWrap);
                propertyWrapsForName.computeIfAbsent(fieldWrap.getName(), k -> new PropertyWrap(k))
                        .setFieldWrap(fieldWrap);
            }
            c = c.getSuperclass();
        }
    }

    protected void loadDeclaredPropertys() {
        for (Method m : typeWrap.getType().getMethods()) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m.getName().length() > 3) {
                if (m.getReturnType() == void.class && m.getParameterCount() == 1) {
                    //setter
                    if (m.getName().startsWith("set")) {
                        PropertyMethodWrap sw = eggg.newPropertyMethodWrap(this, m);

                        propertyWrapsForName.computeIfAbsent(sw.getName(), k -> new PropertyWrap(k))
                                .setSetterWrap(sw);
                    }
                } else if (m.getReturnType() != void.class && m.getParameterCount() == 0) {
                    //getter
                    if (m.getName().startsWith("get")) {
                        PropertyMethodWrap gw = eggg.newPropertyMethodWrap(this, m);

                        propertyWrapsForName.computeIfAbsent(gw.getName(), k -> new PropertyWrap(k))
                                .setGetterWrap(gw);
                    }
                }
            }
        }
    }
}