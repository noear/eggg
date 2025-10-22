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
public class PropertyMethodWrap implements Property {
    private final Method method;
    private final TypeWrap methodTypeWrap;

    private final FieldWrap fieldWrap;

    private final String name;
    private final String alias;
    private final Object digest;

    private final boolean isTransient;
    private final boolean isReadMode;

    private final Eggg eggg;

    public PropertyMethodWrap(Eggg eggg, ClassWrap classWrap, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(classWrap, "classWrap");
        Objects.requireNonNull(method, "property");

        this.eggg = eggg;
        this.method = method;

        if (method.getReturnType() != void.class) {
            //getter
            this.isReadMode = true;
            this.methodTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(method.getGenericReturnType(), getGenericInfo(classWrap.getTypeWrap(), method)));
        } else {
            //setter
            this.isReadMode = false;
            this.methodTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(method.getGenericParameterTypes()[0], getGenericInfo(classWrap.getTypeWrap(), method)));
        }

        this.name = Property.resolvePropertyName(method.getName());
        this.fieldWrap = classWrap.getFieldWrapByName(this.name);

        if (fieldWrap == null) {
            this.isTransient = false;
            this.digest = eggg.findDigest(classWrap, this, method, null);
        } else {
            this.isTransient = fieldWrap.isTransient();
            this.digest = eggg.findDigest(classWrap, this, method, fieldWrap.getDigest());
        }

        this.alias = eggg.findAlias(classWrap, this, digest);
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public Object getValue(Object target) throws Exception {
        if (isReadMode) {
            if (method.isAccessible() == false) {
                method.setAccessible(true);
            }

            return method.invoke(target);
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object target, Object value) throws Exception {
        if (isReadMode == false) {
            if (method.isAccessible() == false) {
                method.setAccessible(true);
            }

            method.invoke(target, value);
        }
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    @Override
    public TypeWrap getTypeWrap() {
        return methodTypeWrap;
    }

    public FieldWrap getFieldWrap() {
        return fieldWrap;
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
    public String toString() {
        return method.toString();
    }

    private Map<String, Type> getGenericInfo(TypeWrap owner, Method method) {
        if (method.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = GenericUtil.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());

            if (superType == null || superType == Object.class) {
                return owner.getGenericInfo();
            } else {
                return getGenericInfo(eggg.getTypeWrap(superType), method);
            }
        }
    }
}