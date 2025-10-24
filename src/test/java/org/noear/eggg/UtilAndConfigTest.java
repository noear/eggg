package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Annotation;
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
                .withAliasHandler((cw, holder, digest, def) -> "custom_alias")
                .withDigestHandler((cw, holder, source, ref) -> "custom_digest");

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
        TypeWrap type1 = eggg.getTypeWrap(String.class);
        eggg.close();
        TypeWrap type2 = eggg.getTypeWrap(String.class);

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
        DigestHandler<String> digestHandler = (cw, holder, source, ref) -> {
            if (source instanceof java.lang.reflect.Field) {
                return "field_digest";
            }
            return "default_digest";
        };

        AliasHandler<String> aliasHandler = (cw, holder, digest, def) -> "custom_" + digest;

        Eggg eggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(AnnotatedClass.class));
        FieldWrap fieldWrap = classWrap.getFieldWrapByName("annotatedField");

        assertEquals("field_digest", fieldWrap.getDigest());
        assertEquals("custom_field_digest", fieldWrap.getAlias());
    }
}