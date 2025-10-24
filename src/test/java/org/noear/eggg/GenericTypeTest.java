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
        TypeEggg typeEggg = eggg.getTypeEggg(type);

        assertNotNull(typeEggg);
        assertTrue(typeEggg.isParameterizedType());
        assertEquals(GenericClass.class, typeEggg.getType());

        ParameterizedType pType = typeEggg.getParameterizedType();
        assertNotNull(pType);
        assertEquals(1, pType.getActualTypeArguments().length);
        assertEquals(String.class, pType.getActualTypeArguments()[0]);
    }

    @Test
    void testGenericFieldType() throws NoSuchFieldException {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringGenericClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("value");

        assertNotNull(fieldEggg);
        TypeEggg fieldTypeEggg = fieldEggg.getTypeEggg();
        assertEquals(String.class, fieldTypeEggg.getType());
    }

    @Test
    void testGenericListFieldType() throws NoSuchFieldException {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringGenericClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("list");

        assertNotNull(fieldEggg);
        TypeEggg fieldTypeEggg = fieldEggg.getTypeEggg();
        assertTrue(fieldTypeEggg.isList());
        assertTrue(fieldTypeEggg.isParameterizedType());
    }

    @Test
    void testNestedGenericType() throws NoSuchFieldException {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(NestedGenericClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("complexMap");

        assertNotNull(fieldEggg);
        TypeEggg fieldTypeEggg = fieldEggg.getTypeEggg();
        assertTrue(fieldTypeEggg.isMap());
        assertTrue(fieldTypeEggg.isParameterizedType());
    }

    @Test
    void testGenericMethodReturnType() throws NoSuchMethodException {
        class TestClass {
            public <T> List<T> getList() { return null; }
        }

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Optional<MethodEggg> methodEggg = classEggg.getPublicMethodEgggs().stream()
                .filter(m -> m.getName().equals("getList"))
                .findFirst();

        assertTrue(methodEggg.isPresent());
        TypeEggg returnType = methodEggg.get().getReturnTypeEggg();
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

        TypeEggg typeEggg = eggg.getTypeEggg(type);
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isList());
    }
}