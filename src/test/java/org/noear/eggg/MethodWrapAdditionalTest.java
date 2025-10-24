package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MethodWrapAdditionalTest {

    private final Eggg eggg = new Eggg();

    static class MethodTestClass {
        public void voidMethod() {}
        public String stringMethod() { return "result"; }
        public int intMethod() { return 42; }
        public List<String> genericMethod() { return null; }

        public void methodWithParams(String param1, int param2) {}
        public String methodWithReturn(String input) { return input; }

        public static void staticMethod() {}
        public final void finalMethod() {}
        private void privateMethod() {}
        protected void protectedMethod() {}

        public void methodWithException() throws Exception {}
        public synchronized void synchronizedMethod() {}

        // Overloaded methods
        public void overloaded() {}
        public void overloaded(String param) {}
    }

    static class GenericMethodClass<T> {
        public T genericMethod(T input) { return input; }
        public List<T> genericListMethod() { return null; }
    }

    static class StringMethodClass extends GenericMethodClass<String> {
    }

    @Test
    void testMethodWrapCreation() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertEquals("stringMethod", methodWrap.getName());
        assertEquals(String.class, methodWrap.getReturnTypeEggg().getType());
        assertSame(method, methodWrap.getMethod());
    }

    @Test
    void testVoidReturnType() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("voidMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertNull(methodWrap.getReturnTypeEggg()); // void return type should be null
    }

    @Test
    void testMethodModifiers() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));

        // Test static method
        Method staticMethod = MethodTestClass.class.getMethod("staticMethod");
        MethodEggg staticMethodWrap = eggg.newMethodWrap(classEggg, staticMethod);
        assertTrue(staticMethodWrap.isStatic());

        // Test final method
        Method finalMethod = MethodTestClass.class.getMethod("finalMethod");
        MethodEggg finalMethodWrap = eggg.newMethodWrap(classEggg, finalMethod);
        assertTrue(finalMethodWrap.isFinal());

        // Test public method
        Method publicMethod = MethodTestClass.class.getMethod("stringMethod");
        MethodEggg publicMethodWrap = eggg.newMethodWrap(classEggg, publicMethod);
        assertTrue(publicMethodWrap.isPublic());
    }

    @Test
    void testPrivateMethod() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getDeclaredMethod("privateMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertFalse(methodWrap.isPublic());
    }

    @Test
    void testMethodWithParameters() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithParams", String.class, int.class);
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertEquals(2, methodWrap.getParamCount());

        List<ParamEggg> params = methodWrap.getParamWrapAry();
        assertEquals(2, params.size());
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());

        // Test parameter access by alias
        ParamEggg param1 = methodWrap.getParamWrapByAlias("param1");
        assertNotNull(param1);
        assertEquals(String.class, param1.getTypeEggg().getType());

        // Test parameter existence check
        assertTrue(methodWrap.hasParamWrapByAlias("param1"));
        assertFalse(methodWrap.hasParamWrapByAlias("nonExistent"));
    }

    @Test
    void testGenericMethod() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringMethodClass.class));
        Method method = GenericMethodClass.class.getMethod("genericMethod", Object.class);
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        TypeEggg returnType = methodWrap.getReturnTypeEggg();
        assertNotNull(returnType);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, returnType.getType());
    }

    @Test
    void testGenericReturnType() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("genericMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        TypeEggg returnType = methodWrap.getReturnTypeEggg();
        assertNotNull(returnType);
        assertTrue(returnType.isList());
    }

    @Test
    void testMethodInvocation() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithReturn", String.class);
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        MethodTestClass instance = new MethodTestClass();
        Object result = methodWrap.newInstance(instance, "testInput");

        assertEquals("testInput", result);
    }

    @Test
    void testStaticMethodInvocation() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("staticMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        // Static method can be invoked with null instance
        methodWrap.newInstance(null);
        // No assertion - just testing that it doesn't throw
    }

    @Test
    void testMethodWithException() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithException");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        // Method declares Exception in throws clause
        assertTrue(method.getExceptionTypes().length > 0);
    }

    @Test
    void testSynchronizedMethod() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("synchronizedMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        assertNotNull(methodWrap);
        assertTrue(Modifier.isSynchronized(method.getModifiers()));
    }

    @Test
    void testOverloadedMethods() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));

        // Find both overloaded methods
        List<MethodEggg> overloadedMethods = classEggg.getPublicMethodWraps().stream()
                .filter(m -> m.getName().equals("overloaded"))
                .collect(Collectors.toList());

        assertEquals(2, overloadedMethods.size());

        // One should have 0 parameters, the other should have 1
        MethodEggg noParam = overloadedMethods.stream()
                .filter(m -> m.getParamCount() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(noParam);

        MethodEggg oneParam = overloadedMethods.stream()
                .filter(m -> m.getParamCount() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(oneParam);
    }

    @Test
    void testMethodToString() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        String toString = methodWrap.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("stringMethod"));
    }

    @Test
    void testMethodDigest() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        Object digest = methodWrap.getDigest();
        assertNull(digest); // No digest handler set
    }

    @Test
    void testMethodHandleInvocation() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithReturn", String.class);
        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);

        MethodTestClass instance = new MethodTestClass();

        // Test that method handle works (if available)
        Object result = methodWrap.newInstance(instance, "methodHandleTest");
        assertEquals("methodHandleTest", result);
    }

//    @Test
//    void testMethodDeclaredFlag() throws Exception {
//        class ParentClass {
//            public void parentMethod() {}
//        }
//
//        class ChildClass extends ParentClass {
//            public void childMethod() {}
//        }
//
//        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ChildClass.class));
//
//        // Child method should be declared
//        MethodWrap childMethod = classEggg.getPublicMethodWraps().stream()
//                .filter(m -> m.getName().equals("childMethod"))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(childMethod);
//        assertTrue(childMethod.isDeclared());
//
//        // Parent method should not be declared in child
//        MethodWrap parentMethod = classEggg.getPublicMethodWraps().stream()
//                .filter(m -> m.getName().equals("parentMethod"))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(parentMethod);
//        assertFalse(parentMethod.isDeclared());
//    }
}