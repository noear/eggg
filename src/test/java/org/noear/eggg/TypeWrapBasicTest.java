package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeWrapBasicTest {

    private final Eggg eggg = new Eggg();

    @Test
    void testBasicTypeWrap() {
        TypeWrap stringType = eggg.getTypeWrap(String.class);
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getType());
        assertTrue(stringType.isString());
        assertFalse(stringType.isNumber());
        assertFalse(stringType.isList());
    }

    @Test
    void testNumberTypeWrap() {
        TypeWrap intType = eggg.getTypeWrap(Integer.class);
        assertNotNull(intType);
        assertEquals(Integer.class, intType.getType());
        assertTrue(intType.isNumber());
        assertFalse(intType.isString());
    }

    @Test
    void testListTypeWrap() {
        TypeWrap listType = eggg.getTypeWrap(List.class);
        assertNotNull(listType);
        assertEquals(List.class, listType.getType());
        assertTrue(listType.isList());
        assertFalse(listType.isMap());
    }

    @Test
    void testMapTypeWrap() {
        TypeWrap mapType = eggg.getTypeWrap(Map.class);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getType());
        assertTrue(mapType.isMap());
        assertFalse(mapType.isList());
    }

    @Test
    void testPrimitiveTypeWrap() {
        TypeWrap intType = eggg.getTypeWrap(int.class);
        assertNotNull(intType);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isNumber());
    }

    @Test
    void testInterfaceTypeWrap() {
        TypeWrap listType = eggg.getTypeWrap(List.class);
        assertNotNull(listType);
        assertTrue(listType.isInterface());
    }

    @Test
    void testArrayTypeWrap() {
        TypeWrap arrayType = eggg.getTypeWrap(String[].class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArray());
    }

    @Test
    void testEnumTypeWrap() {
        TypeWrap enumType = eggg.getTypeWrap(TestEnum.class);
        assertNotNull(enumType);
        assertTrue(enumType.isEnum());
    }

    @Test
    void testGenericInfo() {
        TypeWrap typeWrap = eggg.getTypeWrap(String.class);
        assertNotNull(typeWrap.getGenericInfo());
        assertTrue(typeWrap.getGenericInfo().isEmpty());
    }

    @Test
    void testTypeWrapCaching() {
        TypeWrap type1 = eggg.getTypeWrap(String.class);
        TypeWrap type2 = eggg.getTypeWrap(String.class);
        assertSame(type1, type2, "TypeWrap should be cached");
    }

    enum TestEnum {
        VALUE1, VALUE2
    }
}