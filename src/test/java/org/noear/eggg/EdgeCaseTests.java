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
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(EmptyClass.class));

        assertNotNull(classEggg);
        assertTrue(classEggg.getFieldWraps().isEmpty());
        assertTrue(classEggg.getPropertyWraps().isEmpty());

        // Should have constructor
        assertNotNull(classEggg.getConstrWrap());
    }

    @Test
    void testClassWithOnlyStaticMethods() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithOnlyStaticMethods.class));

        assertNotNull(classEggg);
        assertTrue(classEggg.getFieldWraps().isEmpty());
        assertTrue(classEggg.getPropertyWraps().isEmpty());

        // Should have public methods (including static ones)
        assertFalse(classEggg.getPublicMethodWraps().isEmpty());
    }

    @Test
    void testClassWithOnlyPrivateConstructor() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithOnlyPrivateConstructor.class));

        assertNotNull(classEggg);
        ConstrEggg constrWrap = classEggg.getConstrWrap();
        assertNotNull(constrWrap);

        // Should be able to access private constructor
        Executable executable = constrWrap.getConstr();
        assertNotNull(executable);
        assertTrue(executable instanceof Constructor);
    }

    @Test
    void testObjectClass() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(Object.class));

        assertNotNull(classEggg);
        // Object class should have methods but no fields (in our context)
        assertTrue(classEggg.getPublicMethodWraps().isEmpty());
    }

    @Test
    void testInterface() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(Runnable.class));

        assertNotNull(classEggg);
        assertTrue(classEggg.getTypeEggg().isInterface());
        assertTrue(classEggg.getFieldWraps().isEmpty()); // Interfaces can't have instance fields
    }

    @Test
    void testAnonymousClass() {
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {}
        };

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(anonymous.getClass()));
        assertNotNull(classEggg);
    }

    @Test
    void testArrayClass() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(String[].class));
        assertNotNull(classEggg);
        assertTrue(classEggg.getTypeEggg().isArray());
    }
}