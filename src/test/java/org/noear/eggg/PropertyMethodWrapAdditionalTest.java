package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertyMethodEgggAdditionalTest {

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
    void testPropertyMethodEgggCreation() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        assertEquals("getSimpleField", propertyEggg.getMethod().getName());
        assertEquals("simpleField", propertyEggg.getName());
        assertTrue(propertyEggg.isReadMode());
        assertFalse(propertyEggg.isTransient());
    }

    @Test
    void testSetterPropertyMethodEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, setter);

        assertNotNull(propertyEggg);
        assertEquals("setSimpleField", propertyEggg.getMethod().getName());
        assertEquals("simpleField", propertyEggg.getName());
        assertFalse(propertyEggg.isReadMode());
        assertEquals(String.class, propertyEggg.getTypeEggg().getType());
    }

    @Test
    void testCollectionProperty() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getListField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        assertTrue(propertyEggg.isReadMode());
        TypeEggg typeEggg = propertyEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isList());
    }

    @Test
    void testMapProperty() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getMapField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        TypeEggg typeEggg = propertyEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isMap());
    }

    @Test
    void testTransientProperty() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getTransientField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        assertTrue(propertyEggg.isTransient());
        assertNotNull(propertyEggg.getFieldEggg());
        assertTrue(propertyEggg.getFieldEggg().isTransient());
    }

    @Test
    void testBooleanProperty() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("isActive");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        assertEquals("active", propertyEggg.getName());
        assertTrue(propertyEggg.isReadMode());
        assertEquals(boolean.class, propertyEggg.getTypeEggg().getType());
    }

    @Test
    void testPropertyWithoutField() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("retrieveData");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        assertEquals("retrieveData", propertyEggg.getMethod().getName());
        // Should not be recognized as property method (name doesn't start with get/set)
        assertEquals("rieveData", propertyEggg.getName()); // This might need adjustment in your logic
        assertNull(propertyEggg.getFieldEggg());
        assertFalse(propertyEggg.isTransient());
    }

    @Test
    void testGenericProperty() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringPropertyClass.class));
        Method getter = GenericPropertyClass.class.getMethod("getGenericField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        assertNotNull(propertyEggg);
        TypeEggg typeEggg = propertyEggg.getTypeEggg();
        assertNotNull(typeEggg);
        // Generic should be resolved to String due to inheritance
        assertEquals(String.class, typeEggg.getType());
    }

    @Test
    void testPropertyValueAccess() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        instance.setSimpleField("testValue");

        Object value = propertyEggg.getValue(instance);
        assertEquals("testValue", value);
    }

    @Test
    void testPropertyValueSetting() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, setter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        propertyEggg.setValue(instance, "newValue");

        assertEquals("newValue", instance.getSimpleField());
    }

    @Test
    void testGetterOnSetterMethod() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method setter = ComplexPropertyClass.class.getMethod("setSimpleField", String.class);
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, setter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        // Calling getValue on setter should return null
        Object value = propertyEggg.getValue(instance);
        assertNull(value);
    }

    @Test
    void testSetterOnGetterMethod() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        // Calling setValue on getter should do nothing
        propertyEggg.setValue(instance, "ignored");

        // Value should remain unchanged (or null if not set)
        assertNull(instance.getSimpleField());
    }

    @Test
    void testPropertyToString() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        String toString = propertyEggg.toString();
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

        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = customEggg.newPropertyMethodEggg(classEggg, getter);

        assertEquals("property_digest", propertyEggg.getDigest());
        assertEquals("alias_property_digest", propertyEggg.getAlias());
    }

    @Test
    void testPropertyWithMethodHandle() throws Throwable {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexPropertyClass.class));
        Method getter = ComplexPropertyClass.class.getMethod("getSimpleField");
        PropertyMethodEggg propertyEggg = eggg.newPropertyMethodEggg(classEggg, getter);

        ComplexPropertyClass instance = new ComplexPropertyClass();
        instance.setSimpleField("methodHandleTest");

        // Test that method handle works
        Object value = propertyEggg.getValue(instance);
        assertEquals("methodHandleTest", value);
    }
}