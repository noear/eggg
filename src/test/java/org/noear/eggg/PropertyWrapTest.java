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
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        Collection<PropertyWrap> properties = classWrap.getPropertyWraps();

        assertNotNull(properties);
        assertTrue(properties.size() >= 4);

        assertNotNull(classWrap.getPropertyWrapByName("name"));
        assertNotNull(classWrap.getPropertyWrapByName("age"));
        assertNotNull(classWrap.getPropertyWrapByName("tempData"));
    }

    @Test
    void testPropertyComponents() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap nameProperty = classWrap.getPropertyWrapByName("name");

        assertNotNull(nameProperty);
        assertNotNull(nameProperty.getGetterWrap());
        assertNotNull(nameProperty.getSetterWrap());
        assertNotNull(nameProperty.getFieldWrap());

        assertEquals("name", nameProperty.getName());
    }

    @Test
    void testReadOnlyProperty() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap readOnlyProperty = classWrap.getPropertyWrapByName("readOnly");

        assertNotNull(readOnlyProperty);
        assertNotNull(readOnlyProperty.getGetterWrap());
        assertNull(readOnlyProperty.getSetterWrap());
    }

    @Test
    void testWriteOnlyProperty() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap writeOnlyProperty = classWrap.getPropertyWrapByName("writeOnly");

        assertNotNull(writeOnlyProperty);
        assertNull(writeOnlyProperty.getGetterWrap());
        assertNotNull(writeOnlyProperty.getSetterWrap());
    }

    @Test
    void testTransientProperty() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap tempDataProperty = classWrap.getPropertyWrapByName("tempData");

        assertNotNull(tempDataProperty);
        assertTrue(tempDataProperty.getFieldWrap().isTransient());
    }

    @Test
    void testPropertyAccess() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap nameProperty = classWrap.getPropertyWrapByName("name");

        TestClass instance = new TestClass();
        nameProperty.getSetterWrap().setValue(instance, "John");

        Object value = nameProperty.getGetterWrap().getValue(instance);
        assertEquals("John", value);
    }

    @Test
    void testPropertyMethodWrap() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(TestClass.class));
        PropertyWrap ageProperty = classWrap.getPropertyWrapByName("age");

        PropertyMethodWrap getter = ageProperty.getGetterWrap();
        PropertyMethodWrap setter = ageProperty.getSetterWrap();

        assertNotNull(getter);
        assertNotNull(setter);
        assertEquals("age", getter.getName());
        assertEquals("age", setter.getName());
        assertTrue(getter.isReadMode());
        assertFalse(setter.isReadMode());
    }
}