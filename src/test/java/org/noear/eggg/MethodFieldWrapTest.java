package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodFieldWrapTest {

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
    void testMethodWrapProperties() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("getValue");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertEquals("getValue", methodWrap.getName());
        assertFalse(methodWrap.isStatic());
        assertFalse(methodWrap.isFinal());
        assertTrue(methodWrap.isPublic());
        assertEquals(0, methodWrap.getParamCount());
    }

    @Test
    void testStaticMethodWrap() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("staticMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertTrue(methodWrap.isStatic());
    }

    @Test
    void testFinalMethodWrap() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("finalMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertTrue(methodWrap.isFinal());
    }

    @Test
    void testMethodWithParameters() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("methodWithParams", String.class, int.class);
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertEquals(2, methodWrap.getParamCount());

        List<ParamEggg> params = methodWrap.getParamWrapAry();
        assertEquals(2, params.size());
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());
    }

    @Test
    void testFieldWrapProperties() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldWrap = classEggg.getFieldWrapByName("value");

        assertNotNull(fieldWrap);
        assertEquals("value", fieldWrap.getName());
        assertEquals(String.class, fieldWrap.getTypeEggg().getType());
        assertFalse(fieldWrap.isFinal());
        assertFalse(fieldWrap.isStatic());
        assertTrue(fieldWrap.isPrivate());
    }

    @Test
    void testFinalFieldWrap() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldWrap = classEggg.getFieldWrapByName("finalField");

        assertNotNull(fieldWrap);
        assertTrue(fieldWrap.isFinal());
    }

    @Test
    void testMethodInvocation() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Method method = TestClass.class.getMethod("getValue");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        TestClass instance = new TestClass();
        instance.setValue("test");

        Object result = methodWrap.newInstance(instance);
        assertEquals("test", result);
    }

    @Test
    void testFieldAccess() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        FieldEggg fieldWrap = classEggg.getFieldWrapByName("value");

        TestClass instance = new TestClass();
        fieldWrap.setValue(instance, "testValue");

        Object value = fieldWrap.getValue(instance);
        assertEquals("testValue", value);
    }
}