/*
 * Copyright 2005-2025 noear.org and authors
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
public class PropertyHub<EA extends Object> {
    private final String name;

    private FieldWrap<EA> fieldWrap;
    private PropertyMethodWrap<EA> getterWrap;
    private PropertyMethodWrap<EA> setterWrap;

    public PropertyHub(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public FieldWrap<EA> getFieldWrap() {
        return fieldWrap;
    }

    public PropertyMethodWrap<EA> getGetterWrap() {
        return getterWrap;
    }

    public PropertyMethodWrap<EA> getSetterWrap() {
        return setterWrap;
    }

    /// //////////

    protected void setFieldWrap(FieldWrap<EA> f) {
        this.fieldWrap = f;
    }

    protected void setGetterWrap(PropertyMethodWrap<EA> g) {
        this.getterWrap = g;
    }

    protected void setSetterWrap(PropertyMethodWrap<EA> s) {
        this.setterWrap = s;
    }
}