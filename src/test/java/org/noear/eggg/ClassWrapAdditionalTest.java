package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClassWrapAdditionalTest {

    private final Eggg eggg = new Eggg();

    static class TestClassWithMultipleConstructors {
        private String value;

        public TestClassWithMultipleConstructors() {}
        public TestClassWithMultipleConstructors(String value) { this.value = value; }
        public TestClassWithMultipleConstructors(String value, int number) { this.value = value + number; }

        public String getValue() { return value; }
    }

    static class TestClassWithStaticMethods {
        public static String staticMethod() { return "static"; }
        public static TestClassWithStaticMethods create() { return new TestClassWithStaticMethods(); }
    }

    static class TestClassWithBridgeMethods implements Comparable<TestClassWithBridgeMethods> {
        private int value;

        @Override
        public int compareTo(TestClassWithBridgeMethods o) {
            return Integer.compare(this.value, o.value);
        }

        public void setValue(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    static abstract class AbstractClass {
        public abstract void abstractMethod();
        public void concreteMethod() {}
    }

    @Test
    void testClassWithMultipleConstructors() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClassWithMultipleConstructors.class));
        ConstrEggg constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        // Should select the constructor with least parameters (no-arg constructor)
        assertEquals(0, constrWrap.getParamCount());
    }

    @Test
    void testStaticMethodAsConstructor() {
        // Test with creator annotation simulation
        Eggg customEggg = new Eggg().withCreatorClass(java.lang.Deprecated.class);

        ClassEggg classWrap = customEggg.getClassWrap(customEggg.getTypeWrap(TestClassWithStaticMethods.class));
        ConstrEggg constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
    }

    @Test
    void testBridgeMethodHandling() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClassWithBridgeMethods.class));
        Collection<MethodEggg> declaredMethods = classWrap.getDeclaredMethodWraps();
        Collection<MethodEggg> publicMethods = classWrap.getPublicMethodWraps();

        // Bridge methods should be handled properly
        assertNotNull(declaredMethods);
        assertNotNull(publicMethods);

        // Should find the actual compareTo method
        Optional<MethodEggg> compareToMethod = publicMethods.stream()
                .filter(m -> m.getName().equals("compareTo"))
                .filter(m -> m.getParamCount() == 1)
                .findFirst();
        assertTrue(compareToMethod.isPresent());
    }

    @Test
    void testAbstractClass() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(AbstractClass.class));

        assertNotNull(classWrap);
        assertTrue(classWrap.getTypeWrap().isAbstract());

        Collection<MethodEggg> methods = classWrap.getPublicMethodWraps();
        assertTrue(methods.size() >= 2); // abstractMethod and concreteMethod

        Optional<MethodEggg> abstractMethod = methods.stream()
                .filter(m -> m.getName().equals("abstractMethod"))
                .findFirst();
        assertTrue(abstractMethod.isPresent());
    }

    @Test
    void testFieldAccessByAlias() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClassWithMultipleConstructors.class));

        // Test field access by alias (when alias handler is not set, alias should be null)
        FieldEggg fieldWrap = classWrap.getFieldWrapByAlias("value");
        assertNotNull(fieldWrap); // No alias handler set

        // But should be accessible by name
        fieldWrap = classWrap.getFieldWrapByName("value");
        assertNotNull(fieldWrap);
    }

    @Test
    void testPropertyAccessByAlias() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClassWithMultipleConstructors.class));

        // Test property access by alias
        PropertyEggg propertyWrap = classWrap.getPropertyWrapByAlias("value");
        assertNotNull(propertyWrap); // No alias handler set

        // But should be accessible by name
        propertyWrap = classWrap.getPropertyWrapByName("value");
        assertNotNull(propertyWrap);
    }

    @Test
    void testToStringMethod() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(String.class));
        String toString = classWrap.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("java.lang.String"));
    }

    @Test
    void testDigestRetrieval() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(String.class));
        Object digest = classWrap.getDigest();
        assertNull(digest); // No digest handler set
    }

    @Test
    void testRecordLikeDetection() {
         class RecordLikeClass {
            private final String name;
            private final int age;

            public RecordLikeClass(String name, int age) {
                this.name = name;
                this.age = age;
            }

            public String getName() { return name; }
            public int getAge() { return age; }
        }

        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(RecordLikeClass.class));

        // Should be detected as record-like if all fields are final
        assertTrue(classWrap.isLikeRecordClass());
        assertFalse(classWrap.isRealRecordClass());
    }

    @Test
    void testNonRecordLikeClass() {
         class NonRecordLikeClass {
            private String name; // Not final
            private final int age;

            public  NonRecordLikeClass(String name, int age) {
                this.name = name;
                this.age = age;
            }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public int getAge() { return age; }
        }

        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(NonRecordLikeClass.class));
        assertFalse(classWrap.isLikeRecordClass());
    }

//    @Test
//    void testMethodDeclarationDetection() throws Exception {
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
//        // Test declared methods
//        Optional<MethodWrap> childMethod = classWrap.getDeclaredMethodWraps().stream()
//                .filter(m -> m.getName().equals("childMethod"))
//                .findFirst();
//        assertTrue(childMethod.isPresent());
//        assertTrue(childMethod.get().isDeclared());
//
//        // Test public methods (including inherited)
//        Optional<MethodWrap> parentMethod = classWrap.getPublicMethodWraps().stream()
//                .filter(m -> m.getName().equals("parentMethod"))
//                .findFirst();
//        assertTrue(parentMethod.isPresent());
//        assertFalse(parentMethod.get().isDeclared()); // Not declared in child class
//    }
}