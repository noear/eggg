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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * 属性方法包装器
 *
 * @author noear
 * @since 1.0
 */
public class PropertyMethodWrap<EA extends Object> implements Property<EA> {
    private final Method property;
    private final TypeWrap propertyTypeWrap;

    private final String name;
    private final String alias;
    private final EA attachment;

    private final boolean isReadMode;

    private final Eggg eggg;

    public PropertyMethodWrap(Eggg eggg, ClassWrap owner, Method property) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(property, "property");

        this.eggg = eggg;
        this.property = property;

        if (property.getReturnType() != void.class) {
            //getter
            this.isReadMode = true;
            this.propertyTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(property.getGenericReturnType(), getGenericInfo(owner.getTypeWrap(), property)));
        } else {
            //setter
            this.isReadMode = false;
            this.propertyTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(property.getGenericParameterTypes()[0], getGenericInfo(owner.getTypeWrap(), property)));
        }

        String nameTmp = property.getName().substring(3);
        this.name = nameTmp.substring(0, 1).toLowerCase() + nameTmp.substring(1);

        this.attachment = (EA) eggg.findAttachment(owner, property);
        this.alias = eggg.findAlias(attachment);
    }

    @Override
    public Object getValue(Object target) throws Exception {
        if (isReadMode) {
            if (property.isAccessible() == false) {
                property.setAccessible(true);
            }

            return property.invoke(target);
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object target, Object value) throws Exception {
        if (isReadMode == false) {
            if (property.isAccessible() == false) {
                property.setAccessible(true);
            }

            property.invoke(target, value);
        }
    }

    @Override
    public TypeWrap getTypeWrap() {
        return propertyTypeWrap;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public EA getAttachment() {
        return attachment;
    }

    @Override
    public String toString() {
        return property.toString();
    }

    private Map<String, Type> getGenericInfo(TypeWrap owner, Method method) {
        if (method.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = GenericUtil.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());
            return getGenericInfo(eggg.getTypeWrap(superType), method);
        }
    }
}