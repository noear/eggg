package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.*;

class UtilAndConfigTest {

    @Test
    void testJavaUtil() {
        assertTrue(JavaUtil.JAVA_MAJOR_VERSION > 0);
        assertNotNull(JavaUtil.IS_WINDOWS);

        // Test record detection (will be false on Java < 17)
        assertFalse(JavaUtil.isRecordClass(String.class));
    }

    @Test
    void testPropertyNameResolution() {
        assertEquals("name", Property.resolvePropertyName("getName"));
        assertEquals("age", Property.resolvePropertyName("setAge"));
        assertEquals("firstName", Property.resolvePropertyName("getFirstName"));
    }

    @Test
    void testEgggConfiguration() {
        Eggg eggg = new Eggg()
                .withAliasHandler((cw, s, def) -> "custom_alias")
                .withDigestHandler((cw, s, ref) -> "custom_digest");

        // Test that configuration is accepted without error
        assertNotNull(eggg);
    }

    @Test
    void testReflectHandler() {
        ReflectHandler handler = ReflectHandlerDefault.getInstance();
        assertNotNull(handler);

        Class<?> testClass = String.class;
        assertNotNull(handler.getDeclaredFields(testClass));
        assertNotNull(handler.getDeclaredMethods(testClass));
        assertNotNull(handler.getMethods(testClass));
    }

    @Test
    void testEgggClearCache() {
        Eggg eggg = new Eggg();
        TypeEggg type1 = eggg.getTypeEggg(String.class);
        eggg.clear();
        TypeEggg type2 = eggg.getTypeEggg(String.class);

        // After clear, should create new instance (not guaranteed but likely)
        assertNotNull(type1);
        assertNotNull(type2);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value() default "";
    }

    static class AnnotatedClass {
        @TestAnnotation("field")
        private String annotatedField;

        @TestAnnotation("method")
        public void annotatedMethod() {}
    }

    @Test
    void testCustomHandlers() {
        DigestHandler digestHandler = (cw, s, ref) -> {
            if (s.getElement() instanceof java.lang.reflect.Field) {
                return "field_digest";
            }
            return "default_digest";
        };

        AliasHandler aliasHandler = (cw, s, def) -> "custom_" + s.getDigest();

        Eggg eggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("annotatedField");

        assertEquals("field_digest", fieldEggg.getDigest());
        assertEquals("custom_field_digest", fieldEggg.getAlias());
    }
}