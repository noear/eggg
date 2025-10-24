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


    private FieldEggg fieldWrap;
    private PropertyMethodEggg getterWrap;
    private PropertyMethodEggg setterWrap;

    public PropertyEggg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public FieldEggg getFieldWrap() {
        return fieldWrap;
    }

    public PropertyMethodEggg getGetterWrap() {
        return getterWrap;
    }

    public PropertyMethodEggg getSetterWrap() {
        return setterWrap;
    }

    /// //////////

    protected void setFieldWrap(FieldEggg f) {
        this.fieldWrap = f;
        this.alias = f.getAlias();
    }

    protected void setGetterWrap(PropertyMethodEggg g) {
        this.getterWrap = g;
        this.alias = g.getAlias();
    }

    protected void setSetterWrap(PropertyMethodEggg s) {
        this.setterWrap = s;
        this.alias = s.getAlias();
    }

    @Override
    public String toString() {
        return "PropertyWrap{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", fieldWrap=" + fieldWrap +
                ", getterWrap=" + getterWrap +
                ", setterWrap=" + setterWrap +
                '}';
    }
}