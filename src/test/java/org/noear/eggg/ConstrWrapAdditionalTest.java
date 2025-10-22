package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

import static org.junit.jupiter.api.Assertions.*;

class ConstrWrapAdditionalTest {

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
        ClassWrap classWrap = customEggg.getClassWrap(customEggg.getTypeWrap(ClassWithAnnotatedConstructor.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        assertTrue(constrWrap.isSecurity()); // Should be secure because it has annotation
        assertEquals(1, constrWrap.getParamCount());
    }

    @Test
    void testNoArgConstructorSecurity() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithAnnotatedConstructor.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        assertTrue(constrWrap.isSecurity()); // Should be secure because it has no parameters
        assertEquals(0, constrWrap.getParamCount());
    }

    @Test
    void testMultipleParameterConstructor() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        assertEquals(2, constrWrap.getParamCount());
        assertFalse(constrWrap.isSecurity()); // Not secure because it has parameters and no annotation

        // Test parameter access
        ParamWrap param1 = constrWrap.getParamWrapByAlias("param1");
        assertNotNull(param1);
        assertEquals(String.class, param1.getTypeWrap().getType());

        ParamWrap param2 = constrWrap.getParamWrapByAlias("param2");
        assertNotNull(param2);
        assertEquals(int.class, param2.getTypeWrap().getType());
    }

    @Test
    void testParameterAliasAccess() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);

        // Test parameter access by alias
        ParamWrap param = constrWrap.getParamWrapByAlias("param1");
        assertNotNull(param);

        // Test non-existent alias
        ParamWrap nonExistent = constrWrap.getParamWrapByAlias("nonExistent");
        assertNull(nonExistent);

        // Test alias existence check
        assertTrue(constrWrap.hasParamWrapByAlias("param1"));
        assertFalse(constrWrap.hasParamWrapByAlias("nonExistent"));
    }

    @Test
    void testConstructorInvocation() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);

        // Test instance creation
        Object instance = constrWrap.newInstance("test", 42);
        assertNotNull(instance);
        assertTrue(instance instanceof ClassWithMultipleParams);

        ClassWithMultipleParams typedInstance = (ClassWithMultipleParams) instance;
        assertEquals("test", typedInstance.getParam1());
        assertEquals(42, typedInstance.getParam2());
    }

    @Test
    void testPrivateConstructor() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithPrivateConstructor.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        // Should still find private constructor
        assertNotNull(constrWrap);
    }

    @Test
    void testConstructorDigest() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        Object digest = constrWrap.getDigest();
        assertNull(digest); // No digest handler set
    }

    @Test
    void testConstructorToString() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        String toString = constrWrap.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ClassWithMultipleParams"));
    }

    @Test
    void testConstructorExecutableAccess() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        Executable executable = constrWrap.getConstr();
        assertNotNull(executable);
        assertTrue(executable instanceof Constructor);
    }

    @Test
    void testParameterListAccess() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithMultipleParams.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);

        // Test parameter list access
        java.util.List<ParamWrap> params = constrWrap.getParamWrapAry();
        assertNotNull(params);
        assertEquals(2, params.size());

        // Verify parameter order
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());
    }
}