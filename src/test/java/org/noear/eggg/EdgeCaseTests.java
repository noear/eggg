package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTests {

    private final Eggg eggg = new Eggg();

    static class EmptyClass {
    }

    static class ClassWithOnlyStaticMethods {
        public static void staticMethod() {}
        private static void privateStaticMethod() {}
    }

    static class ClassWithOnlyPrivateConstructor {
        private ClassWithOnlyPrivateConstructor() {}
    }

    @Test
    void testEmptyClass() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(EmptyClass.class));

        assertNotNull(classWrap);
        assertTrue(classWrap.getFieldWraps().isEmpty());
        assertTrue(classWrap.getPropertyWraps().isEmpty());

        // Should have constructor
        assertNotNull(classWrap.getConstrWrap());
    }

    @Test
    void testClassWithOnlyStaticMethods() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithOnlyStaticMethods.class));

        assertNotNull(classWrap);
        assertTrue(classWrap.getFieldWraps().isEmpty());
        assertTrue(classWrap.getPropertyWraps().isEmpty());

        // Should have public methods (including static ones)
        assertFalse(classWrap.getPublicMethodWraps().isEmpty());
    }

    @Test
    void testClassWithOnlyPrivateConstructor() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ClassWithOnlyPrivateConstructor.class));

        assertNotNull(classWrap);
        ConstrWrap constrWrap = classWrap.getConstrWrap();
        assertNotNull(constrWrap);

        // Should be able to access private constructor
        Executable executable = constrWrap.getConstr();
        assertNotNull(executable);
        assertTrue(executable instanceof Constructor);
    }

    @Test
    void testObjectClass() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(Object.class));

        assertNotNull(classWrap);
        // Object class should have methods but no fields (in our context)
        assertTrue(classWrap.getPublicMethodWraps().isEmpty());
    }

    @Test
    void testInterface() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(Runnable.class));

        assertNotNull(classWrap);
        assertTrue(classWrap.getTypeWrap().isInterface());
        assertTrue(classWrap.getFieldWraps().isEmpty()); // Interfaces can't have instance fields
    }

    @Test
    void testAnonymousClass() {
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {}
        };

        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(anonymous.getClass()));
        assertNotNull(classWrap);
    }

    @Test
    void testArrayClass() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(String[].class));
        assertNotNull(classWrap);
        assertTrue(classWrap.getTypeWrap().isArray());
    }
}