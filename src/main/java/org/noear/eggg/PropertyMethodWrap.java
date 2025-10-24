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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 属性方法包装器
 *
 * @author noear
 * @since 1.0
 */
public class PropertyMethodWrap implements Property {
    private final Method method;
    private MethodHandle methodHandle;

    private final TypeWrap propertyTypeWrap;

    private final FieldWrap fieldWrap;

    private final String name;
    private final String alias;
    private final Object digest;

    public PropertyMethodWrap(Eggg eggg, ClassWrap classWrap, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(classWrap, "classWrap");
        Objects.requireNonNull(method, "property");

        this.method = method;

        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            this.methodHandle = null;
        }

        if (method.getReturnType() != void.class) {
            //getter
            this.propertyTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(method.getGenericReturnType(), eggg.getMethodGenericInfo(classWrap.getTypeWrap(), method)));
        } else {
            //setter
            this.propertyTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(method.getGenericParameterTypes()[0], eggg.getMethodGenericInfo(classWrap.getTypeWrap(), method)));
        }

        this.name = Property.resolvePropertyName(method.getName());
        this.fieldWrap = classWrap.getFieldWrapByName(this.name);

        if (fieldWrap == null) {
            this.digest = eggg.findDigest(classWrap, this, method, null);
        } else {
            this.digest = eggg.findDigest(classWrap, this, method, fieldWrap.getDigest());
        }

        this.alias = eggg.findAlias(classWrap, this, digest, name);
    }

    @Override
    public boolean isTransient() {
        return fieldWrap != null && fieldWrap.isTransient();
    }

    public boolean isReadMode() {
        return method.getReturnType() != void.class;
    }

    @Override
    public Object getValue(Object target) throws Throwable {
        if (isReadMode()) {
            if (methodHandle == null) {
                if (method.isAccessible() == false) {
                    method.setAccessible(true);
                }

                return method.invoke(target);
            } else {
                return methodHandle.bindTo(target).invokeWithArguments();
            }
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object target, Object value) throws Throwable {
        if (isReadMode() == false) {
            if (methodHandle == null) {
                if (method.isAccessible() == false) {
                    method.setAccessible(true);
                }

                method.invoke(target, value);
            } else {
                methodHandle.bindTo(target).invokeWithArguments(value);
            }
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
        return propertyTypeWrap;
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
}