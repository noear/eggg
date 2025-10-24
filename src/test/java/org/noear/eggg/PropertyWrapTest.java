package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PropertyWrapTest {

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
        Collection<PropertyEggg> properties = classEggg.getPropertyWraps();

        assertNotNull(properties);
        assertTrue(properties.size() >= 4);

        assertNotNull(classEggg.getPropertyWrapByName("name"));
        assertNotNull(classEggg.getPropertyWrapByName("age"));
        assertNotNull(classEggg.getPropertyWrapByName("tempData"));
    }

    @Test
    void testPropertyComponents() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg nameProperty = classEggg.getPropertyWrapByName("name");

        assertNotNull(nameProperty);
        assertNotNull(nameProperty.getGetterWrap());
        assertNotNull(nameProperty.getSetterWrap());
        assertNotNull(nameProperty.getFieldWrap());

        assertEquals("name", nameProperty.getName());
    }

    @Test
    void testReadOnlyProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg readOnlyProperty = classEggg.getPropertyWrapByName("readOnly");

        assertNotNull(readOnlyProperty);
        assertNotNull(readOnlyProperty.getGetterWrap());
        assertNull(readOnlyProperty.getSetterWrap());
    }

    @Test
    void testWriteOnlyProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg writeOnlyProperty = classEggg.getPropertyWrapByName("writeOnly");

        assertNotNull(writeOnlyProperty);
        assertNull(writeOnlyProperty.getGetterWrap());
        assertNotNull(writeOnlyProperty.getSetterWrap());
    }

    @Test
    void testTransientProperty() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg tempDataProperty = classEggg.getPropertyWrapByName("tempData");

        assertNotNull(tempDataProperty);
        assertTrue(tempDataProperty.getFieldWrap().isTransient());
    }

    @Test
    void testPropertyAccess() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg nameProperty = classEggg.getPropertyWrapByName("name");

        TestClass instance = new TestClass();
        nameProperty.getSetterWrap().setValue(instance, "John");

        Object value = nameProperty.getGetterWrap().getValue(instance);
        assertEquals("John", value);
    }

    @Test
    void testPropertyMethodWrap() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(TestClass.class));
        PropertyEggg ageProperty = classEggg.getPropertyWrapByName("age");

        PropertyMethodEggg getter = ageProperty.getGetterWrap();
        PropertyMethodEggg setter = ageProperty.getSetterWrap();

        assertNotNull(getter);
        assertNotNull(setter);
        assertEquals("age", getter.getName());
        assertEquals("age", setter.getName());
        assertTrue(getter.isReadMode());
        assertFalse(setter.isReadMode());
    }
}