package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeWrapBasicTest {

    private final Eggg eggg = new Eggg();

    @Test
    void testBasicTypeWrap() {
        TypeEggg stringType = eggg.getTypeWrap(String.class);
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getType());
        assertTrue(stringType.isString());
        assertFalse(stringType.isNumber());
        assertFalse(stringType.isList());
    }

    @Test
    void testNumberTypeWrap() {
        TypeEggg intType = eggg.getTypeWrap(Integer.class);
        assertNotNull(intType);
        assertEquals(Integer.class, intType.getType());
        assertTrue(intType.isNumber());
        assertFalse(intType.isString());
    }

    @Test
    void testListTypeWrap() {
        TypeEggg listType = eggg.getTypeWrap(List.class);
        assertNotNull(listType);
        assertEquals(List.class, listType.getType());
        assertTrue(listType.isList());
        assertFalse(listType.isMap());
    }

    @Test
    void testMapTypeWrap() {
        TypeEggg mapType = eggg.getTypeWrap(Map.class);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getType());
        assertTrue(mapType.isMap());
        assertFalse(mapType.isList());
    }

    @Test
    void testPrimitiveTypeWrap() {
        TypeEggg intType = eggg.getTypeWrap(int.class);
        assertNotNull(intType);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isNumber());
    }

    @Test
    void testInterfaceTypeWrap() {
        TypeEggg listType = eggg.getTypeWrap(List.class);
        assertNotNull(listType);
        assertTrue(listType.isInterface());
    }

    @Test
    void testArrayTypeWrap() {
        TypeEggg arrayType = eggg.getTypeWrap(String[].class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArray());
    }

    @Test
    void testEnumTypeWrap() {
        TypeEggg enumType = eggg.getTypeWrap(TestEnum.class);
        assertNotNull(enumType);
        assertTrue(enumType.isEnum());
    }

    @Test
    void testGenericInfo() {
        TypeEggg typeWrap = eggg.getTypeWrap(String.class);
        assertNotNull(typeWrap.getGenericInfo());
        assertTrue(typeWrap.getGenericInfo().isEmpty());
    }

    @Test
    void testTypeWrapCaching() {
        TypeEggg type1 = eggg.getTypeWrap(String.class);
        TypeEggg type2 = eggg.getTypeWrap(String.class);
        assertSame(type1, type2, "TypeWrap should be cached");
    }

    enum TestEnum {
        VALUE1, VALUE2
    }
}