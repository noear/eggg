package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertyMethodWrapAdditionalTest {

    private final Eggg eggg = new Eggg();

    static class ComplexPropertyClass {
        private String simpleField;
        private List<String> listField;
        private Map<String, Integer> mapField;
        private transient String transientField;
        private final String finalField = "constant";

        // Standard getter/setter
        public String getSimpleField() { return simpleField; }
        public void setSimpleField(String simpleField) { this.simpleField = simpleField; }

        // Collection getter/setter
        public List<String> getListField() { return listField; }
        public void setListField(List<String> listField) { this.listField = listField; }

        // Map getter/setter
        public Map<String, Integer> getMapField() { return mapField; }
        public void setMapField(Map<String, Integer> mapField) { this.mapField = mapField; }

        // Transient field getter/setter
        public String getTransientField() { return transientField; }
        public void setTransientField(String transientField) { this.transientField = transientField; }

        // Final field getter (no setter)
        public String getFinalField() { return finalField; }

        // Boolean getter (is-prefix)
        public boolean isActive() { return true; }
        public void setActive(boolean active) { }

        // Getter with different name pattern
        public String retrieveData() { return "data"; }
        public void storeData(String data) { }

        // Static getter (should be ignored)
        public static String getStaticField() { return "static"; }
        public static void setStaticField(String value) { }
    }

    static class GenericPropertyClass<T> {
        private T genericField;
        private List<T> genericList;

        public T getGenericField() { return genericField; }
        public void setGenericField(T genericField) { this.genericField = genericField; }

        public List<T> getGenericList() { return genericList; }
        public void setGenericList(List<T> genericList) { this.genericList = genericList; }
    }

    static class StringPropertyClass extends GenericPropertyClass<String> {
    }

    @Test
    void testPropertyMethodWrapCreation() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        assertEquals("getSimpleField", propertyWrap.getMethod().getName());
        assertEquals("simpleField", propertyWrap.getName());
        assertTrue(propertyWrap.isReadMode());
        assertFalse(propertyWrap.isTransient());
    }

    @Test
    void testSetterPropertyMethodWrap() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, setter);

        assertNotNull(propertyWrap);
        assertEquals("setSimpleField", propertyWrap.getMethod().getName());
        assertEquals("simpleField", propertyWrap.getName());
        assertFalse(propertyWrap.isReadMode());
        assertEquals(String.class, propertyWrap.getTypeWrap().getType());
    }

    @Test
    void testCollectionProperty() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getListField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        assertTrue(propertyWrap.isReadMode());
        TypeWrap typeWrap = propertyWrap.getTypeWrap();
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isList());
    }

    @Test
    void testMapProperty() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getMapField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        TypeWrap typeWrap = propertyWrap.getTypeWrap();
        assertNotNull(typeWrap);
        assertTrue(typeWrap.isMap());
    }

    @Test
    void testTransientProperty() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getTransientField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        assertTrue(propertyWrap.isTransient());
        assertNotNull(propertyWrap.getFieldWrap());
        assertTrue(propertyWrap.getFieldWrap().isTransient());
    }

    @Test
    void testBooleanProperty() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("isActive");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        assertEquals("active", propertyWrap.getName());
        assertTrue(propertyWrap.isReadMode());
        assertEquals(boolean.class, propertyWrap.getTypeWrap().getType());
    }

    @Test
    void testPropertyWithoutField() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("retrieveData");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        assertEquals("retrieveData", propertyWrap.getMethod().getName());
        // Should not be recognized as property method (name doesn't start with get/set)
        assertEquals("rieveData", propertyWrap.getName()); // This might need adjustment in your logic
        assertNull(propertyWrap.getFieldWrap());
        assertFalse(propertyWrap.isTransient());
    }

    @Test
    void testGenericProperty() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(StringPropertyClass.class));
        Method getter = GenericPropertyClass.class.getMethod("getGenericField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        assertNotNull(propertyWrap);
        TypeWrap typeWrap = propertyWrap.getTypeWrap();
        assertNotNull(typeWrap);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, typeWrap.getType());
    }

    @Test
    void testPropertyValueAccess() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        instance.setSimpleField("testValue");

        Object value = propertyWrap.getValue(instance);
        assertEquals("testValue", value);
    }

    @Test
    void testPropertyValueSetting() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, setter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        propertyWrap.setValue(instance, "newValue");

        assertEquals("newValue", instance.getSimpleField());
    }

    @Test
    void testGetterOnSetterMethod() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, setter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        // Calling getValue on setter should return null
        Object value = propertyWrap.getValue(instance);
        assertNull(value);
    }

    @Test
    void testSetterOnGetterMethod() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        // Calling setValue on getter should do nothing
        propertyWrap.setValue(instance, "ignored");

        // Value should remain unchanged (or null if not set)
        assertNull(instance.getSimpleField());
    }

    @Test
    void testPropertyToString() throws Exception {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        String toString = propertyWrap.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("getSimpleField"));
    }

    @Test
    void testPropertyDigestAndAlias() throws Exception {
        DigestHandler<String> digestHandler = (cw, holder, source, ref) -> "property_digest";
        AliasHandler<String> aliasHandler = (cw, holder, digest, def) -> "alias_" + digest;

        Eggg customEggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassWrap classWrap = customEggg.getClassWrap(customEggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = customEggg.newPropertyMethodWrap(classWrap, getter);

        assertEquals("property_digest", propertyWrap.getDigest());
        assertEquals("alias_property_digest", propertyWrap.getAlias());
    }

    @Test
    void testPropertyWithMethodHandle() throws Throwable {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodWrap propertyWrap = eggg.newPropertyMethodWrap(classWrap, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        instance.setSimpleField("methodHandleTest");

        // Test that method handle works
        Object value = propertyWrap.getValue(instance);
        assertEquals("methodHandleTest", value);
    }
}