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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * 字段包装器
 *
 * @author noear
 * @since 1.0
 */
public class FieldEggg implements Property {
    private final ClassEggg ownerEggg;

    private final Field field;
    private final TypeEggg fieldTypeEggg;


    protected PropertyMethodEggg getterEggg;
    protected PropertyMethodEggg setterEggg;

    private final String name;
    private final String alias;
    private final Object digest;

    public FieldEggg(Eggg eggg, ClassEggg ownerEggg, Field field) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(ownerEggg, "ownerEggg");
        Objects.requireNonNull(field, "field");

        this.ownerEggg = ownerEggg;

        this.field = field;
        this.fieldTypeEggg = eggg.getTypeEggg(eggg.reviewType(field.getGenericType(), eggg.getFieldGenericInfo(ownerEggg.getTypeEggg(), field)));

        this.name = field.getName();
        this.digest = eggg.findDigest(ownerEggg, this, field, null);
        this.alias = eggg.findAlias(ownerEggg, this, digest, name);
    }

    public ClassEggg getOwnerEggg() {
        return ownerEggg;
    }

    public Field getField() {
        return field;
    }

    @Override
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    /**
     * 只读的
     */
    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 静态的
     */
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 私有的
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(field.getModifiers());
    }

    /**
     * 私有的
     */
    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }

    /**
     * 临时的（不需要执久化）
     */
    @Override
    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
    }

    @Override
    public Object getValue(Object target) {
        try {
            if (field.isAccessible() == false) {
                field.setAccessible(true);
            }

            return field.get(target);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object getValue(Object target, boolean allowGetter) {
        if (allowGetter && getterEggg != null) {
            return getterEggg.getValue(target);
        } else {
            return getValue(target);
        }
    }

    @Override
    public void setValue(Object target, Object value) {
        if (isFinal() == false) {
            try {
                if (field.isAccessible() == false) {
                    field.setAccessible(true);
                }

                field.set(target, value);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void setValue(Object target, Object value, boolean allowSetter) {
        if (allowSetter && setterEggg != null) {
            setterEggg.setValue(target, value);
        } else {
            setValue(target, value);
        }
    }

    @Override
    public TypeEggg getTypeEggg() {
        return fieldTypeEggg;
    }

    public Class<?> getType() {
        return fieldTypeEggg.getType();
    }

    public Type getGenericType() {
        return fieldTypeEggg.getGenericType();
    }

    public Map<String, Type> getGenericInfo() {
        return fieldTypeEggg.getGenericInfo();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    private Annotation[] annotations;

    public Annotation[] getAnnotations() {
        if (annotations == null) {
            annotations = field.getAnnotations();
        }
        return annotations;
    }

    @Override
    public String toString() {
        return field.toString();
    }
}