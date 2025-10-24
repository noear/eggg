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
public class PropertyMethodEggg implements Property {
    private final ClassEggg ownerEggg;

    private final Method method;
    private MethodHandle methodHandle;

    private final TypeEggg propertyTypeEggg;

    private final FieldEggg fieldEggg;

    private final String name;
    private final String alias;
    private final Object digest;

    public PropertyMethodEggg(Eggg eggg, ClassEggg ownerEggg, Method method) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(ownerEggg, "ownerEggg");
        Objects.requireNonNull(method, "method");

        this.ownerEggg = ownerEggg;
        this.method = method;

        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (Throwable e) {
            this.methodHandle = null;
        }

        if (method.getReturnType() != void.class) {
            //getter
            this.propertyTypeEggg = eggg.getTypeEggg(eggg.reviewType(method.getGenericReturnType(), eggg.getMethodGenericInfo(ownerEggg.getTypeEggg(), method)));
        } else {
            //setter
            this.propertyTypeEggg = eggg.getTypeEggg(eggg.reviewType(method.getGenericParameterTypes()[0], eggg.getMethodGenericInfo(ownerEggg.getTypeEggg(), method)));
        }

        this.name = Property.resolvePropertyName(method.getName());
        this.fieldEggg = ownerEggg.getFieldEgggByName(this.name);

        if (fieldEggg == null) {
            this.digest = eggg.findDigest(ownerEggg, this, method, null);
        } else {
            this.digest = eggg.findDigest(ownerEggg, this, method, fieldEggg.getDigest());
        }

        this.alias = eggg.findAlias(ownerEggg, this, digest, name);
    }

    public ClassEggg getOwnerEggg() {
        return ownerEggg;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public boolean isTransient() {
        return fieldEggg != null && fieldEggg.isTransient();
    }

    public boolean isReadMode() {
        return method.getReturnType() != void.class;
    }

    @Override
    public Object getValue(Object target) {
        if (isReadMode()) {
            try {
                if (methodHandle == null) {
                    if (method.isAccessible() == false) {
                        method.setAccessible(true);
                    }

                    return method.invoke(target);
                } else {
                    return methodHandle.bindTo(target).invokeWithArguments();
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object target, Object value) {
        if (isReadMode() == false) {
            try {
                if (methodHandle == null) {
                    if (method.isAccessible() == false) {
                        method.setAccessible(true);
                    }

                    method.invoke(target, value);
                } else {
                    methodHandle.bindTo(target).invokeWithArguments(value);
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    @Override
    public TypeEggg getTypeEggg() {
        return propertyTypeEggg;
    }

    public FieldEggg getFieldEggg() {
        return fieldEggg;
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