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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 字段包装器
 *
 * @author noear
 * @since 1.0
 */
public class FieldWrap<A> implements Property<A> {
    private final Field field;
    private final TypeWrap fieldTypeWrap;

    private final String name;
    private final A attachment;

    private boolean isFinal;
    private boolean isTransient;

    private Eggg eggg;

    public FieldWrap(Eggg eggg, ClassWrap classWrap, Field field) {
        this.eggg = eggg;
        this.field = field;
        this.fieldTypeWrap = eggg.getTypeWrap(GenericUtil.reviewType(field.getGenericType(), getGenericInfo(classWrap.getTypeWrap(), field)));

        this.isFinal = Modifier.isFinal(field.getModifiers());
        this.isTransient = Modifier.isTransient(field.getModifiers());

        this.name = field.getName();
        this.attachment = (A) eggg.findAttachment(classWrap, field);
    }

    public Field getField() {
        return field;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isTransient() {
        return isTransient;
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
        if (isFinal == false) {
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
    public A getAttachment() {
        return attachment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return field.toString();
    }

    private Map<String, Type> getGenericInfo(TypeWrap owner, Field field) {
        if (field.getDeclaringClass() == owner.getType()) {
            return owner.getGenericInfo();
        } else {
            Type superType = GenericUtil.reviewType(owner.getType().getGenericSuperclass(), owner.getGenericInfo());
            return getGenericInfo(eggg.getTypeWrap(superType), field);
        }
    }
}