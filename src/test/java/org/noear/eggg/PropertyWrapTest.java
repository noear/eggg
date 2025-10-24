package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PropertyEgggTest {

    private final Eggg eggg = new Eggg();

    static class TestClass {
        private String name;
        private int age;
        private transient String tempData;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getTempData() { return tempData; }
        public void setTempData(String tempData) { this.tempData = tempData; }

        // Read-only property
        public String getReadOnly() { return "readonly"; }

        // Write-only property
        public void setWriteOnly(String value) { }
    }

    @Test
    void testPropertyDetection() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        Collection<PropertyEggg> properties = classEggg.getPropertyEgggs();

        assertNotNull(properties);
        assertTrue(properties.size() >= 4);

        assertNotNull(classEggg.getPropertyEgggByName("name"));
        assertNotNull(classEggg.getPropertyEgggByName("age"));
        assertNotNull(classEggg.getPropertyEgggByName("tempData"));
    }

    @Test
    void testPropertyComponents() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg nameProperty = classEggg.getPropertyEgggByName("name");

        assertNotNull(nameProperty);
        assertNotNull(nameProperty.getGetterEggg());
        assertNotNull(nameProperty.getSetterEggg());
        assertNotNull(nameProperty.getFieldEggg());

        assertEquals("name", nameProperty.getName());
    }

    @Test
    void testReadOnlyProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg readOnlyProperty = classEggg.getPropertyEgggByName("readOnly");

        assertNotNull(readOnlyProperty);
        assertNotNull(readOnlyProperty.getGetterEggg());
        assertNull(readOnlyProperty.getSetterEggg());
    }

    @Test
    void testWriteOnlyProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg writeOnlyProperty = classEggg.getPropertyEgggByName("writeOnly");

        assertNotNull(writeOnlyProperty);
        assertNull(writeOnlyProperty.getGetterEggg());
        assertNotNull(writeOnlyProperty.getSetterEggg());
    }

    @Test
    void testTransientProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg tempDataProperty = classEggg.getPropertyEgggByName("tempData");

        assertNotNull(tempDataProperty);
        assertTrue(tempDataProperty.getFieldEggg().isTransient());
    }

    @Test
    void testPropertyAccess() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg nameProperty = classEggg.getPropertyEgggByName("name");

        TestClass instance = new TestClass();
        nameProperty.getSetterEggg().setValue(instance, "John");

        Object value = nameProperty.getGetterEggg().getValue(instance);
        assertEquals("John", value);
    }

    @Test
    void testPropertyMethodEggg() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg ageProperty = classEggg.getPropertyEgggByName("age");

        PropertyMethodEggg getter = ageProperty.getGetterEggg();
        PropertyMethodEggg setter = ageProperty.getSetterEggg();

        assertNotNull(getter);
        assertNotNull(setter);
        assertEquals("age", getter.getName());
        assertEquals("age", setter.getName());
        assertTrue(getter.isReadMode());
        assertFalse(setter.isReadMode());
    }
}