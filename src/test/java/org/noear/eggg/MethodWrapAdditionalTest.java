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
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        assertEquals("stringMethod", methodWrap.getName());
        assertEquals(String.class, methodWrap.getReturnTypeWrap().getType());
        assertSame(method, methodWrap.getMethod());
    }

    @Test
    void testVoidReturnType() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("voidMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        assertNull(methodWrap.getReturnTypeWrap()); // void return type should be null
    }

    @Test
    void testMethodModifiers() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));

        // Test static method
        Method staticMethod = MethodTestClass.class.getMethod("staticMethod");
        MethodWrap staticMethodWrap = eggg.newMethodWrap(classWrap, staticMethod);
        assertTrue(staticMethodWrap.isStatic());

        // Test final method
        Method finalMethod = MethodTestClass.class.getMethod("finalMethod");
        MethodWrap finalMethodWrap = eggg.newMethodWrap(classWrap, finalMethod);
        assertTrue(finalMethodWrap.isFinal());

        // Test public method
        Method publicMethod = MethodTestClass.class.getMethod("stringMethod");
        MethodWrap publicMethodWrap = eggg.newMethodWrap(classWrap, publicMethod);
        assertTrue(publicMethodWrap.isPublic());
    }

    @Test
    void testPrivateMethod() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getDeclaredMethod("privateMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        assertFalse(methodWrap.isPublic());
    }

    @Test
    void testMethodWithParameters() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithParams", String.class, int.class);
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        assertEquals(2, methodWrap.getParamCount());

        List<ParamWrap> params = methodWrap.getParamWrapAry();
        assertEquals(2, params.size());
        assertEquals("param1", params.get(0).getName());
        assertEquals("param2", params.get(1).getName());

        // Test parameter access by alias
        ParamWrap param1 = methodWrap.getParamWrapByAlias("param1");
        assertNotNull(param1);
        assertEquals(String.class, param1.getTypeWrap().getType());

        // Test parameter existence check
        assertTrue(methodWrap.hasParamWrapByAlias("param1"));
        assertFalse(methodWrap.hasParamWrapByAlias("nonExistent"));
    }

    @Test
    void testGenericMethod() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(StringMethodClass.class));
        Method method = GenericMethodClass.class.getMethod("genericMethod", Object.class);
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        TypeWrap returnType = methodWrap.getReturnTypeWrap();
        assertNotNull(returnType);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, returnType.getType());
    }

    @Test
    void testGenericReturnType() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("genericMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        TypeWrap returnType = methodWrap.getReturnTypeWrap();
        assertNotNull(returnType);
        assertTrue(returnType.isList());
    }

    @Test
    void testMethodInvocation() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithReturn", String.class);
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        MethodTestClass instance = new MethodTestClass();
        Object result = methodWrap.newInstance(instance, "testInput");

        assertEquals("testInput", result);
    }

    @Test
    void testStaticMethodInvocation() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("staticMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        // Static method can be invoked with null instance
        methodWrap.newInstance(null);
        // No assertion - just testing that it doesn't throw
    }

    @Test
    void testMethodWithException() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithException");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        // Method declares Exception in throws clause
        assertTrue(method.getExceptionTypes().length > 0);
    }

    @Test
    void testSynchronizedMethod() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("synchronizedMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        assertNotNull(methodWrap);
        assertTrue(Modifier.isSynchronized(method.getModifiers()));
    }

    @Test
    void testOverloadedMethods() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));

        // Find both overloaded methods
        List<MethodWrap> overloadedMethods = classWrap.getPublicMethodWraps().stream()
                .filter(m -> m.getName().equals("overloaded"))
                .collect(Collectors.toList());

        assertEquals(2, overloadedMethods.size());

        // One should have 0 parameters, the other should have 1
        MethodWrap noParam = overloadedMethods.stream()
                .filter(m -> m.getParamCount() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(noParam);

        MethodWrap oneParam = overloadedMethods.stream()
                .filter(m -> m.getParamCount() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(oneParam);
    }

    @Test
    void testMethodToString() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        String toString = methodWrap.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("stringMethod"));
    }

    @Test
    void testMethodDigest() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("stringMethod");
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

        Object digest = methodWrap.getDigest();
        assertNull(digest); // No digest handler set
    }

    @Test
    void testMethodHandleInvocation() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(MethodTestClass.class));
        Method method = MethodTestClass.class.getMethod("methodWithReturn", String.class);
        MethodWrap methodWrap = eggg.newMethodWrap(classWrap, method);

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
//        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ChildClass.class));
//
//        // Child method should be declared
//        MethodWrap childMethod = classWrap.getPublicMethodWraps().stream()
//                .filter(m -> m.getName().equals("childMethod"))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(childMethod);
//        assertTrue(childMethod.isDeclared());
//
//        // Parent method should not be declared in child
//        MethodWrap parentMethod = classWrap.getPublicMethodWraps().stream()
//                .filter(m -> m.getName().equals("parentMethod"))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(parentMethod);
//        assertFalse(parentMethod.isDeclared());
//    }
}