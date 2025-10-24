package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GenericTypeTest {

    private final Eggg eggg = new Eggg();

    static class GenericClass<T> {
        public T value;
        public List<T> list;
    }

    static class StringGenericClass extends GenericClass<String> {
    }

    static class NestedGenericClass<K, V> {
        public Map<K, List<V>> complexMap;
    }

    @Test
    void testParameterizedType() {
        Type type = StringGenericClass.class.getGenericSuperclass();
        TypeEggg typeWrap = eggg.getTypeWrap(type);

        assertNotNull(typeWrap);
        assertTrue(typeWrap.isParameterizedType());
        assertEquals(GenericClass.class, typeWrap.getType());

        ParameterizedType pType = typeWrap.getParameterizedType();
        assertNotNull(pType);
        assertEquals(1, pType.getActualTypeArguments().length);
        assertEquals(String.class, pType.getActualTypeArguments()[0]);
    }

    @Test
    void testGenericFieldType() throws NoSuchFieldException {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(StringGenericClass.class));
        FieldEggg fieldWrap = classWrap.getFieldWrapByName("value");

        assertNotNull(fieldWrap);
        TypeEggg fieldTypeWrap = fieldWrap.getTypeWrap();
        assertEquals(String.class, fieldTypeWrap.getType());
    }

    @Test
    void testGenericListFieldType() throws NoSuchFieldException {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(StringGenericClass.class));
        FieldEggg fieldWrap = classWrap.getFieldWrapByName("list");

        assertNotNull(fieldWrap);
        TypeEggg fieldTypeWrap = fieldWrap.getTypeWrap();
        assertTrue(fieldTypeWrap.isList());
        assertTrue(fieldTypeWrap.isParameterizedType());
    }

    @Test
    void testNestedGenericType() throws NoSuchFieldException {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(NestedGenericClass.class));
        FieldEggg fieldWrap = classWrap.getFieldWrapByName("complexMap");

        assertNotNull(fieldWrap);
        TypeEggg fieldTypeWrap = fieldWrap.getTypeWrap();
        assertTrue(fieldTypeWrap.isMap());
        assertTrue(fieldTypeWrap.isParameterizedType());
    }

    @Test
    void testGenericMethodReturnType() throws NoSuchMethodException {
        class TestClass {
            public <T> List<T> getList() { return null; }
        }

        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        Optional<MethodEggg> methodWrap = classWrap.getPublicMethodWraps().stream()
                .filter(m -> m.getName().equals("getList"))
                .findFirst();

        assertTrue(methodWrap.isPresent());
        TypeEggg returnType = methodWrap.get().getReturnTypeWrap();
        assertNotNull(returnType);
        assertTrue(returnType.isList());
    }

    @Test
    void testWildcardType() {
        Type type = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[] {
                        new WildcardType() {
                            public Type[] getUpperBounds() { return new Type[] { Number.class }; }
                            public Type[] getLowerBounds() { return new Type[0]; }
                            public String getTypeName() { return "? extends java.lang.Number"; }
                        }
                };
            }
            public Type getRawType() { return List.class; }
            public Type getOwnerType() { return null; }
            public String getTypeName() { return "java.util.List<? extends java.lang.Number>"; }
        };

        TypeEggg typeWrap = eggg.getTypeWrap(type);
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isList());
    }
}