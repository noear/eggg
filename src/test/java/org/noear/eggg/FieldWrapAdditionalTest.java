package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FieldWrapAdditionalTest {

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
    void testFieldWrapCreation() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("publicField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        assertEquals("publicField", fieldWrap.getName());
        assertEquals(String.class, fieldWrap.getTypeWrap().getType());
        assertSame(field, fieldWrap.getField());
    }

    @Test
    void testFieldAccessModifiers() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));

        // Test public field
        Field publicField = FieldTestClass.class.getField("publicField");
        FieldWrap publicFieldWrap = eggg.newFieldWrap(classWrap, publicField);
        assertTrue(publicFieldWrap.isPublic());
        assertFalse(publicFieldWrap.isPrivate());

        // Test private field
        Field privateField = FieldTestClass.class.getDeclaredField("privateField");
        FieldWrap privateFieldWrap = eggg.newFieldWrap(classWrap, privateField);
        assertTrue(privateFieldWrap.isPrivate());
        assertFalse(privateFieldWrap.isPublic());
    }

    @Test
    void testFinalField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("finalField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        assertTrue(fieldWrap.isFinal());

        // Test that setting value on final field doesn't throw but doesn't change value
        FieldTestClass instance = new FieldTestClass();
        try {
            fieldWrap.setValue(instance, "newValue");
            // Should not change due to final modifier
            assertEquals("constant", fieldWrap.getValue(instance));
        } catch (Exception e) {
            // Some JVMs might throw exception for final field modification
            assertTrue(e instanceof IllegalAccessException);
        }
    }

    @Test
    void testStaticField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("staticField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        assertTrue(fieldWrap.isStatic());
    }

    @Test
    void testTransientField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("transientField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        assertTrue(fieldWrap.isTransient());
    }

    @Test
    void testVolatileField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("volatileField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        // Note: Your FieldWrap doesn't currently track volatile modifier
        // This test verifies it doesn't break on volatile fields
    }

    @Test
    void testGenericField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(StringFieldClass.class));
        Field field = GenericFieldClass.class.getDeclaredField("genericField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        TypeWrap typeWrap = fieldWrap.getTypeWrap();
        assertNotNull(typeWrap);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, typeWrap.getType());
    }

    @Test
    void testListField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringList");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        TypeWrap typeWrap = fieldWrap.getTypeWrap();
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isList());
    }

    @Test
    void testMapField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringIntMap");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        TypeWrap typeWrap = fieldWrap.getTypeWrap();
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isMap());
    }

    @Test
    void testArrayField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("stringArray");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        assertNotNull(fieldWrap);
        TypeWrap typeWrap = fieldWrap.getTypeWrap();
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isArray());
    }

    @Test
    void testFieldValueAccess() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        FieldTestClass instance = new FieldTestClass();
        instance.publicField = "testValue";

        Object value = fieldWrap.getValue(instance);
        assertEquals("testValue", value);
    }

    @Test
    void testFieldValueSetting() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        FieldTestClass instance = new FieldTestClass();
        fieldWrap.setValue(instance, "newValue");

        assertEquals("newValue", instance.publicField);
    }

    @Test
    void testPrivateFieldAccess() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getDeclaredField("privateField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        FieldTestClass instance = new FieldTestClass();

        // Should be able to access private field through reflection
        fieldWrap.setValue(instance, "privateValue");
        Object value = fieldWrap.getValue(instance);
        assertEquals("privateValue", value);
    }

    @Test
    void testFieldToString() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldWrap fieldWrap = eggg.newFieldWrap(classWrap, field);

        String toString = fieldWrap.toString();
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

        ClassWrap classWrap = customEggg.getClassWrap(customEggg.getTypeWrap(FieldTestClass.class));
        Field field = FieldTestClass.class.getField("publicField");
        FieldWrap fieldWrap = customEggg.newFieldWrap(classWrap, field);

        assertEquals("field_digest", fieldWrap.getDigest());
        assertEquals("alias_field_digest", fieldWrap.getAlias());
    }

    @Test
    void testFieldDeclaredFlag() throws Exception {
        class ParentClass {
            public String parentField;
        }

        class ChildClass extends ParentClass {
            public String childField;
        }

        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ChildClass.class));

        // Child field should be declared
        FieldWrap childField = classWrap.getFieldWrapByName("childField");
        assertNotNull(childField);
        assertTrue(childField.isDeclared());

        // Parent field should not be declared in child
        FieldWrap parentField = classWrap.getFieldWrapByName("parentField");
        assertNotNull(parentField);
        assertFalse(parentField.isDeclared());
    }
}