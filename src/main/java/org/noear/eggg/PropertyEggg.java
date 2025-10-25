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

/**
 * 属性集成
 *
 * @author noear
 * @since 1.0
 */
public class PropertyEggg {
    private final String name;
    private String alias;


    private FieldEggg fieldEggg; //属性，允许没有字段
    private PropertyMethodEggg getterEggg;
    private PropertyMethodEggg setterEggg;

    public PropertyEggg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public FieldEggg getFieldEggg() {
        return fieldEggg;
    }

    public PropertyMethodEggg getGetterEggg() {
        return getterEggg;
    }

    public PropertyMethodEggg getSetterEggg() {
        return setterEggg;
    }


    /**
     * 获取值
     */
    public Object getValue(Object target, boolean allowGetter) {
        if (allowGetter && getterEggg != null) {
            return getterEggg.getValue(target);
        }

        return fieldEggg.getValue(target);
    }

    /**
     * 设置值
     */
    public void setValue(Object target, Object value, boolean allowSetter) {
        if (allowSetter && setterEggg != null) {
            setterEggg.setValue(target, value);
        } else {
            fieldEggg.setValue(target, value);
        }
    }


    /// //////////

    protected void setFieldEggg(FieldEggg f) {
        this.fieldEggg = f;
        this.alias = f.getAlias();
    }

    protected void setGetterEggg(PropertyMethodEggg g) {
        this.getterEggg = g;
        this.alias = g.getAlias();

        if (fieldEggg != null) {
            fieldEggg.getterEggg = g;
        }
    }

    protected void setSetterEggg(PropertyMethodEggg s) {
        this.setterEggg = s;
        this.alias = s.getAlias();

        if (fieldEggg != null) {
            fieldEggg.setterEggg = s;
        }
    }

    @Override
    public String toString() {
        return "PropertyEggg{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", fieldEggg=" + fieldEggg +
                ", getterEggg=" + getterEggg +
                ", setterEggg=" + setterEggg +
                '}';
    }
}