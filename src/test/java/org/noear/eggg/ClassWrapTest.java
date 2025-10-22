package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassWrapTest {

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
    void testClassWrapCreation() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        assertNotNull(classWrap);
        assertEquals(TestClass.class, classWrap.getTypeWrap().getType());
    }

    @Test
    void testFieldWraps() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        Collection<FieldWrap> fields = classWrap.getFieldWraps();

        assertNotNull(fields);
        assertTrue(fields.size() >= 3);

        FieldWrap field1 = classWrap.getFieldWrapByName("field1");
        assertNotNull(field1);
        assertEquals("field1", field1.getName());
        assertEquals(String.class, field1.getTypeWrap().getType());
    }

    @Test
    void testMethodWraps() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        Collection<MethodWrap> publicMethods = classWrap.getPublicMethodWraps();
        Collection<MethodWrap> declaredMethods = classWrap.getDeclaredMethodWraps();

        assertNotNull(publicMethods);
        assertNotNull(declaredMethods);
        assertTrue(publicMethods.size() > 0);
        assertTrue(declaredMethods.size() > 0);

        Optional<MethodWrap> getterMethod = publicMethods.stream()
                .filter(m -> m.getName().equals("getField1"))
                .findFirst();
        assertTrue(getterMethod.isPresent());
    }

    @Test
    void testPropertyWraps() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        Collection<PropertyWrap> properties = classWrap.getPropertyWraps();

        assertNotNull(properties);
        assertTrue(properties.size() >= 2);

        PropertyWrap property1 = classWrap.getPropertyWrapByName("field1");
        assertNotNull(property1);
        assertNotNull(property1.getGetterWrap());
        assertNotNull(property1.getSetterWrap());
    }

    @Test
    void testConstructorWrap() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        ConstrWrap constrWrap = classWrap.getConstrWrap();

        assertNotNull(constrWrap);
        assertTrue(constrWrap.isSecurity());
    }

    @Test
    void testLikeRecordClass() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        assertFalse(classWrap.isLikeRecordClass());
        assertFalse(classWrap.isRealRecordClass());
    }

    @Test
    void testClassWrapCaching() {
        TypeWrap typeWrap = eggg.getTypeWrap(TestClass.class);
        ClassWrap classWrap1 = eggg.getClassWrap(typeWrap);
        ClassWrap classWrap2 = eggg.getClassWrap(typeWrap);

        assertSame(classWrap1, classWrap2, "ClassWrap should be cached");
    }
}