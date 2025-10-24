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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 字段包装器
 *
 * @author noear
 * @since 1.0
 */
public class FieldWrap implements Property {
    private final Field field;
    private final TypeWrap fieldTypeWrap;

    private final String name;
    private final String alias;
    private final Object digest;

    public FieldWrap(Eggg eggg, ClassWrap classWrap, Field field) {
        this.field = field;
        this.fieldTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(field.getGenericType(), eggg.getFieldGenericInfo(classWrap.getTypeWrap(), field)));

        this.name = field.getName();
        this.digest = eggg.findDigest(classWrap, this, field, null);
        this.alias = eggg.findAlias(classWrap, this, digest, name);
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
    public Object getValue(Object target) throws Exception {
        if (field.isAccessible() == false) {
            field.setAccessible(true);
        }

        return field.get(target);
    }

    @Override
    public void setValue(Object target, Object value) throws Exception {
        if (isFinal() == false) {
            if (field.isAccessible() == false) {
                field.setAccessible(true);
            }

            field.set(target, value);
        }
    }

    @Override
    public TypeWrap getTypeWrap() {
        return fieldTypeWrap;
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
        return field.toString();
    }
}