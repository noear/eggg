package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeEgggBasicTest {

    private final Eggg eggg = new Eggg();

    @Test
    void testBasicTypeEggg() {
        TypeEggg stringType = eggg.getTypeEggg(String.class);
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getType());
        assertTrue(stringType.isString());
        assertFalse(stringType.isNumber());
        assertFalse(stringType.isList());
    }

    @Test
    void testNumberTypeEggg() {
        TypeEggg intType = eggg.getTypeEggg(Integer.class);
        assertNotNull(intType);
        assertEquals(Integer.class, intType.getType());
        assertTrue(intType.isNumber());
        assertFalse(intType.isString());
    }

    @Test
    void testListTypeEggg() {
        TypeEggg listType = eggg.getTypeEggg(List.class);
        assertNotNull(listType);
        assertEquals(List.class, listType.getType());
        assertTrue(listType.isList());
        assertFalse(listType.isMap());
    }

    @Test
    void testMapTypeEggg() {
        TypeEggg mapType = eggg.getTypeEggg(Map.class);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getType());
        assertTrue(mapType.isMap());
        assertFalse(mapType.isList());
    }

    @Test
    void testPrimitiveTypeEggg() {
        TypeEggg intType = eggg.getTypeEggg(int.class);
        assertNotNull(intType);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isNumber());
    }

    @Test
    void testInterfaceTypeEggg() {
        TypeEggg listType = eggg.getTypeEggg(List.class);
        assertNotNull(listType);
        assertTrue(listType.isInterface());
    }

    @Test
    void testArrayTypeEggg() {
        TypeEggg arrayType = eggg.getTypeEggg(String[].class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArray());
    }

    @Test
    void testEnumTypeEggg() {
        TypeEggg enumType = eggg.getTypeEggg(TestEnum.class);
        assertNotNull(enumType);
        assertTrue(enumType.isEnum());
    }

    @Test
    void testGenericInfo() {
        TypeEggg typeEggg = eggg.getTypeEggg(String.class);
        assertNotNull(typeEggg.getGenericInfo());
        assertTrue(typeEggg.getGenericInfo().isEmpty());
    }

    @Test
    void testTypeEgggCaching() {
        TypeEggg type1 = eggg.getTypeEggg(String.class);
        TypeEggg type2 = eggg.getTypeEggg(String.class);
        assertSame(type1, type2, "TypeEggg should be cached");
    }

    enum TestEnum {
        VALUE1, VALUE2
    }
}