package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodFieldEgggTest {

    private final Eggg eggg = new Eggg();

    static class TestClass {
        private String value;
        public final String finalField = "constant";

        public TestClass() {}

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public static void staticMethod() {}
        public final void finalMethod() {}
        public String methodWithParams(String param1, int param2) { return ""; }
    }

    @Test
    void testMethodEgggProperties() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("getValue");
        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);

        assertNotNull(methodEggg);
        assertEquals("getValue", methodEggg.getName());
        assertFalse(methodEggg.isStatic());
        assertFalse(methodEggg.isFinal());
        assertTrue(methodEggg.isPublic());
        assertEquals(0, methodEggg.getParamCount());
    }

    @Test
    void testStaticMethodEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("staticMethod");
        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);

        assertNotNull(methodEggg);
        assertTrue(methodEggg.isStatic());
    }

    @Test
    void testFinalMethodEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("finalMethod");
        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);

        assertNotNull(methodEggg);
        assertTrue(methodEggg.isFinal());
    }

    @Test
    void testMethodWithParameters() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("methodWithParams", String.class, int.class);
        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);

        assertNotNull(methodEggg);
        assertEquals(2, methodEggg.getParamCount());

        List<ParamEggg> params = methodEggg.getParamEgggAry();
        assertEquals(2, params.size());
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());
    }

    @Test
    void testFieldEgggProperties() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("value");

        assertNotNull(fieldEggg);
        assertEquals("value", fieldEggg.getName());
        assertEquals(String.class, fieldEggg.getTypeEggg().getType());
        assertFalse(fieldEggg.isFinal());
        assertFalse(fieldEggg.isStatic());
        assertTrue(fieldEggg.isPrivate());
    }

    @Test
    void testFinalFieldEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("finalField");

        assertNotNull(fieldEggg);
        assertTrue(fieldEggg.isFinal());
    }

    @Test
    void testMethodInvocation() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("getValue");
        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);

        TestClass instance = new TestClass();
        instance.setValue("test");

        Object result = methodEggg.invoke(instance);
        assertEquals("test", result);
    }

    @Test
    void testFieldAccess() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldEggg = classEggg.getFieldEgggByName("value");

        TestClass instance = new TestClass();
        fieldEggg.setValue(instance, "testValue");

        Object value = fieldEggg.getValue(instance);
        assertEquals("testValue", value);
    }
}