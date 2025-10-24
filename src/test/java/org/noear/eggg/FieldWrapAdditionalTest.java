package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FieldEgggAdditionalTest {

    private final Eggg eggg = new Eggg();

    static class FieldTestClass {
        public String publicField;
        private String privateField;
        protected String protectedField;
        final String finalField = "constant";
        static String staticField;
        transient String transientField;
        volatile String volatileField;

        // Generic fields
        private List<String> stringList;
        private Map<String, Integer> stringIntMap;
        private String[] stringArray;
    }

    static class GenericFieldClass<T> {
        private T genericField;
        private List<T> genericList;
    }

    static class StringFieldClass extends GenericFieldClass<String> {
    }

    @Test
    void testFieldEgggCreation() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("publicField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        assertEquals("publicField", fieldEggg.getName());
        assertEquals(String.class, fieldEggg.getTypeEggg().getType());
        assertSame(field, fieldEggg.getField());
    }

    @Test
    void testFieldAccessModifiers() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));

        // Test public field
        Field publicField = FieldTestClass.class.getField("publicField");
        FieldEggg publicFieldEggg = eggg.newFieldEggg(classEggg, publicField);
        assertTrue(publicFieldEggg.isPublic());
        assertFalse(publicFieldEggg.isPrivate());

        // Test private field
        Field privateField = FieldTestClass.class.getDeclaredField("privateField");
        FieldEggg privateFieldEggg = eggg.newFieldEggg(classEggg, privateField);
        assertTrue(privateFieldEggg.isPrivate());
        assertFalse(privateFieldEggg.isPublic());
    }

    @Test
    void testFinalField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("finalField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        assertTrue(fieldEggg.isFinal());

        // Test that setting value on final field doesn't throw but doesn't change value
        FieldTestClass instance = new FieldTestClass();
        try {
            fieldEggg.setValue(instance, "newValue");
            // Should not change due to final modifier
            assertEquals("constant", fieldEggg.getValue(instance));
        } catch (Exception e) {
            // Some JVMs might throw exception for final field modification
            assertTrue(e instanceof IllegalAccessException);
        }
    }

    @Test
    void testStaticField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("staticField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        assertTrue(fieldEggg.isStatic());
    }

    @Test
    void testTransientField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("transientField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        assertTrue(fieldEggg.isTransient());
    }

    @Test
    void testVolatileField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("volatileField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        // Note: Your FieldEggg doesn't currently track volatile modifier
        // This test verifies it doesn't break on volatile fields
    }

    @Test
    void testGenericField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringFieldClass.class));
        Field field = GenericFieldClass.class.getDeclaredField("genericField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        TypeEggg typeEggg = fieldEggg.getTypeEggg();
        assertNotNull(typeEggg);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, typeEggg.getType());
    }

    @Test
    void testListField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringList");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        TypeEggg typeEggg = fieldEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isList());
    }

    @Test
    void testMapField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringIntMap");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        TypeEggg typeEggg = fieldEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isMap());
    }

    @Test
    void testArrayField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringArray");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        assertNotNull(fieldEggg);
        TypeEggg typeEggg = fieldEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isArray());
    }

    @Test
    void testFieldValueAccess() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        FieldTestClass instance = new FieldTestClass();
        instance.publicField = "testValue";

        Object value = fieldEggg.getValue(instance);
        assertEquals("testValue", value);
    }

    @Test
    void testFieldValueSetting() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        FieldTestClass instance = new FieldTestClass();
        fieldEggg.setValue(instance, "newValue");

        assertEquals("newValue", instance.publicField);
    }

    @Test
    void testPrivateFieldAccess() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("privateField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        FieldTestClass instance = new FieldTestClass();

        // Should be able to access private field through reflection
        fieldEggg.setValue(instance, "privateValue");
        Object value = fieldEggg.getValue(instance);
        assertEquals("privateValue", value);
    }

    @Test
    void testFieldToString() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldEggg fieldEggg = eggg.newFieldEggg(classEggg, field);

        String toString = fieldEggg.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("publicField"));
    }

    @Test
    void testFieldDigestAndAlias() throws Exception {
        DigestHandler<String> digestHandler = (cw, holder, source, ref) -> "field_digest";
        AliasHandler<String> aliasHandler = (cw, holder, digest, def) -> "alias_" + digest;

        Eggg customEggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldEggg fieldEggg = customEggg.newFieldEggg(classEggg, field);

        assertEquals("field_digest", fieldEggg.getDigest());
        assertEquals("alias_field_digest", fieldEggg.getAlias());
    }

//    @Test
//    void testFieldDeclaredFlag() throws Exception {
//        class ParentClass {
//            public String parentField;
//        }
//
//        class ChildClass extends ParentClass {
//            public String childField;
//        }
//
//        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ChildClass.class));
//
//        // Child field should be declared
//        FieldEggg childField = classEggg.getFieldEgggByName("childField");
//        assertNotNull(childField);
//        assertTrue(childField.isDeclared());
//
//        // Parent field should not be declared in child
//        FieldEggg parentField = classEggg.getFieldEgggByName("parentField");
//        assertNotNull(parentField);
//        assertFalse(parentField.isDeclared());
//    }
}