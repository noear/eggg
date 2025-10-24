package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

import static org.junit.jupiter.api.Assertions.*;

class ConstrEgggAdditionalTest {

    private final Eggg eggg = new Eggg();

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestCreator {
    }

    static class ClassWithAnnotatedConstructor {
        private String value;

        @TestCreator
        public ClassWithAnnotatedConstructor(String value) {
            this.value = value;
        }

        public ClassWithAnnotatedConstructor() {
            this("default");
        }

        public String getValue() { return value; }
    }

    static class ClassWithMultipleParams {
        private String param1;
        private int param2;

        public ClassWithMultipleParams(String param1, int param2) {
            this.param1 = param1;
            this.param2 = param2;
        }

        public String getParam1() { return param1; }
        public int getParam2() { return param2; }
    }

    static class ClassWithPrivateConstructor {
        private String value;

        private ClassWithPrivateConstructor(String value) {
            this.value = value;
        }

        public static ClassWithPrivateConstructor create(String value) {
            return new ClassWithPrivateConstructor(value);
        }

        public String getValue() { return value; }
    }

    @Test
    void testAnnotatedConstructorSecurity() {
        Eggg customEggg = new Eggg().withCreatorClass(TestCreator.class);
        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(ClassWithAnnotatedConstructor.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        assertTrue(constrEggg.isSecurity()); // Should be secure because it has annotation
        assertEquals(1, constrEggg.getParamCount());
    }

    @Test
    void testNoArgConstructorSecurity() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithAnnotatedConstructor.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        assertTrue(constrEggg.isSecurity()); // Should be secure because it has no parameters
        assertEquals(0, constrEggg.getParamCount());
    }

    @Test
    void testMultipleParameterConstructor() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        assertEquals(2, constrEggg.getParamCount());
        assertFalse(constrEggg.isSecurity()); // Not secure because it has parameters and no annotation

        // Test parameter access
        ParamEggg param1 = constrEggg.getParamEgggByAlias("param1");
        assertNotNull(param1);
        assertEquals(String.class, param1.getTypeEggg().getType());

        ParamEggg param2 = constrEggg.getParamEgggByAlias("param2");
        assertNotNull(param2);
        assertEquals(int.class, param2.getTypeEggg().getType());
    }

    @Test
    void testParameterAliasAccess() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);

        // Test parameter access by alias
        ParamEggg param = constrEggg.getParamEgggByAlias("param1");
        assertNotNull(param);

        // Test non-existent alias
        ParamEggg nonExistent = constrEggg.getParamEgggByAlias("nonExistent");
        assertNull(nonExistent);

        // Test alias existence check
        assertTrue(constrEggg.hasParamEgggByAlias("param1"));
        assertFalse(constrEggg.hasParamEgggByAlias("nonExistent"));
    }

    @Test
    void testConstructorInvocation() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);

        // Test instance creation
        Object instance = constrEggg.newInstance("test", 42);
        assertNotNull(instance);
        assertTrue(instance instanceof ClassWithMultipleParams);

        ClassWithMultipleParams typedInstance = (ClassWithMultipleParams) instance;
        assertEquals("test", typedInstance.getParam1());
        assertEquals(42, typedInstance.getParam2());
    }

    @Test
    void testPrivateConstructor() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithPrivateConstructor.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        // Should still find private constructor
        assertNotNull(constrEggg);
    }

    @Test
    void testConstructorDigest() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        Object digest = constrEggg.getDigest();
        assertNull(digest); // No digest handler set
    }

    @Test
    void testConstructorToString() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        String toString = constrEggg.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ClassWithMultipleParams"));
    }

    @Test
    void testConstructorExecutableAccess() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        Executable executable = constrEggg.getConstr();
        assertNotNull(executable);
        assertTrue(executable instanceof Constructor);
    }

    @Test
    void testParameterListAccess() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithMultipleParams.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);

        // Test parameter list access
        java.util.List<ParamEggg> params = constrEggg.getParamEgggAry();
        assertNotNull(params);
        assertEquals(2, params.size());

        // Verify parameter order
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());
    }
}