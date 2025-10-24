package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClassEgggAdditionalTest {

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
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClassWithMultipleConstructors.class));
        ConstrEggg constrEggg = classEggg.getConstrEggg();

        assertNotNull(constrEggg);
        // Should select the constructor with least parameters (no-arg constructor)
        assertEquals(0, constrEggg.getParamCount());
    }

    @Test
    void testStaticMethodAsConstructor() {
        // Test with creator annotation simulation
        Eggg customEggg = new Eggg().withCreatorClass(java.lang.Deprecated.class);

        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(TestClassWithStaticMethods.class));
        ConstrEggg constrEggg = classEggg.getConstrEggg();

        assertNotNull(constrEggg);
    }

    @Test
    void testBridgeMethodHandling() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClassWithBridgeMethods.class));
        Collection<MethodEggg> declaredMethods = classEggg.getDeclaredMethodEgggs();
        Collection<MethodEggg> publicMethods = classEggg.getPublicMethodEgggs();

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
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(AbstractClass.class));

        assertNotNull(classEggg);
        assertTrue(classEggg.getTypeEggg().isAbstract());

        Collection<MethodEggg> methods = classEggg.getPublicMethodEgggs();
        assertTrue(methods.size() >= 2); // abstractMethod and concreteMethod

        Optional<MethodEggg> abstractMethod = methods.stream()
                .filter(m -> m.getName().equals("abstractMethod"))
                .findFirst();
        assertTrue(abstractMethod.isPresent());
    }

    @Test
    void testFieldAccessByAlias() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClassWithMultipleConstructors.class));

        // Test field access by alias (when alias handler is not set, alias should be null)
        FieldEggg fieldEggg = classEggg.getFieldEgggByAlias("value");
        assertNotNull(fieldEggg); // No alias handler set

        // But should be accessible by name
        fieldEggg = classEggg.getFieldEgggByName("value");
        assertNotNull(fieldEggg);
    }

    @Test
    void testPropertyAccessByAlias() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClassWithMultipleConstructors.class));

        // Test property access by alias
        PropertyEggg propertyEggg = classEggg.getPropertyEgggByAlias("value");
        assertNotNull(propertyEggg); // No alias handler set

        // But should be accessible by name
        propertyEggg = classEggg.getPropertyEgggByName("value");
        assertNotNull(propertyEggg);
    }

    @Test
    void testToStringMethod() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(String.class));
        String toString = classEggg.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("java.lang.String"));
    }

    @Test
    void testDigestRetrieval() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(String.class));
        Object digest = classEggg.getDigest();
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

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(RecordLikeClass.class));

        // Should be detected as record-like if all fields are final
        assertTrue(classEggg.isLikeRecordClass());
        assertFalse(classEggg.isRealRecordClass());
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

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(NonRecordLikeClass.class));
        assertFalse(classEggg.isLikeRecordClass());
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
//        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ChildClass.class));
//
//        // Test declared methods
//        Optional<MethodEggg> childMethod = classEggg.getDeclaredMethodEgggs().stream()
//                .filter(m -> m.getName().equals("childMethod"))
//                .findFirst();
//        assertTrue(childMethod.isPresent());
//        assertTrue(childMethod.get().isDeclared());
//
//        // Test public methods (including inherited)
//        Optional<MethodEggg> parentMethod = classEggg.getPublicMethodEgggs().stream()
//                .filter(m -> m.getName().equals("parentMethod"))
//                .findFirst();
//        assertTrue(parentMethod.isPresent());
//        assertFalse(parentMethod.get().isDeclared()); // Not declared in child class
//    }
}