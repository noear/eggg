package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassEgggTest {

    private final Eggg eggg = new Eggg();

    static class TestClass {
        private String field1;
        public int field2;
        protected List<String> field3;

        public TestClass() {}
        public TestClass(String field1) { this.field1 = field1; }

        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public int getField2() { return field2; }
        public void method1() {}
        public String method2(int param) { return ""; }
    }

    @Test
    void testClassEgggCreation() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        assertNotNull(classEggg);
        assertEquals(TestClass.class, classEggg.getTypeEggg().getType());
    }

    @Test
    void testFieldEgggs() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Collection<FieldEggg> fields = classEggg.getFieldEgggs();

        assertNotNull(fields);
        assertTrue(fields.size() >= 3);

        FieldEggg field1 = classEggg.getFieldEgggByName("field1");
        assertNotNull(field1);
        assertEquals("field1", field1.getName());
        assertEquals(String.class, field1.getTypeEggg().getType());
    }

    @Test
    void testMethodEgggs() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Collection<MethodEggg> publicMethods = classEggg.getPublicMethodEgggs();
        Collection<MethodEggg> declaredMethods = classEggg.getDeclaredMethodEgggs();

        assertNotNull(publicMethods);
        assertNotNull(declaredMethods);
        assertTrue(publicMethods.size() > 0);
        assertTrue(declaredMethods.size() > 0);

        Optional<MethodEggg> getterMethod = publicMethods.stream()
                .filter(m -> m.getName().equals("getField1"))
                .findFirst();
        assertTrue(getterMethod.isPresent());
    }

    @Test
    void testPropertyEgggs() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Collection<PropertyEggg> properties = classEggg.getPropertyEgggs();

        assertNotNull(properties);
        assertTrue(properties.size() >= 2);

        PropertyEggg property1 = classEggg.getPropertyEgggByName("field1");
        assertNotNull(property1);
        assertNotNull(property1.getGetterEggg());
        assertNotNull(property1.getSetterEggg());
    }

    @Test
    void testConstructorEggg() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        ConstrEggg constrEggg = classEggg.getConstrEggg();

        assertNotNull(constrEggg);
        assertTrue(constrEggg.isSecurity());
    }

    @Test
    void testLikeRecordClass() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        assertFalse(classEggg.isLikeRecordClass());
        assertFalse(classEggg.isRealRecordClass());
    }

    @Test
    void testClassEgggCaching() {
        TypeEggg typeEggg = eggg.getTypeEggg(TestClass.class);
        ClassEggg classEggg1 = eggg.getClassEggg(typeEggg);
        ClassEggg classEggg2 = eggg.getClassEggg(typeEggg);

        assertSame(classEggg1, classEggg2, "ClassEggg should be cached");
    }
}