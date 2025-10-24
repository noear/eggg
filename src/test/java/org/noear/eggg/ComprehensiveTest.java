package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComprehensiveTest {

    private final Eggg eggg = new Eggg();

    static class ComplexGenericClass<T, U extends Number> {
        private T genericField;
        private List<U> numberList;
        private Map<T, List<U>> complexMap;

        public ComplexGenericClass() {}

        public T getGenericField() { return genericField; }
        public void setGenericField(T genericField) { this.genericField = genericField; }

        public List<U> getNumberList() { return numberList; }
        public void setNumberList(List<U> numberList) { this.numberList = numberList; }

        public Map<T, List<U>> getComplexMap() { return complexMap; }
        public void setComplexMap(Map<T, List<U>> complexMap) { this.complexMap = complexMap; }

        public <V> V processData(T input, U number, V defaultValue) { return defaultValue; }
    }

    static class ConcreteClass extends ComplexGenericClass<String, Integer> {
        private String concreteField;

        public String getConcreteField() { return concreteField; }
        public void setConcreteField(String concreteField) { this.concreteField = concreteField; }
    }

    @Test
    void testComplexGenericInheritance() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ConcreteClass.class));

        // Test fields
        FieldEggg genericField = classEggg.getFieldEgggByName("genericField");
        assertNotNull(genericField);
        assertEquals(String.class, genericField.getTypeEggg().getType());

        FieldEggg numberListField = classEggg.getFieldEgggByName("numberList");
        assertNotNull(numberListField);
        assertTrue(numberListField.getTypeEggg().isList());

        // Test properties
        PropertyEggg genericProperty = classEggg.getPropertyEgggByName("genericField");
        assertNotNull(genericProperty);
        assertNotNull(genericProperty.getGetterEggg());
        assertNotNull(genericProperty.getSetterEggg());
    }

    @Test
    void testGenericMethodInComplexClass() {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ComplexGenericClass.class));

        Optional<MethodEggg> processMethod = classEggg.getPublicMethodEgggs().stream()
                .filter(m -> m.getName().equals("processData"))
                .findFirst();

        assertTrue(processMethod.isPresent());
        MethodEggg methodEggg = processMethod.get();
        assertEquals(3, methodEggg.getParamCount());

        List<ParamEggg> params = methodEggg.getParamEgggAry();
        assertNotNull(params);
        assertEquals(3, params.size());
    }

    @Test
    void testPerformanceWithCaching() {
        long startTime = System.currentTimeMillis();

        // Repeated access should be fast due to caching
        for (int i = 0; i < 100; i++) {
            TypeEggg typeEggg = eggg.getTypeEggg(String.class);
            ClassEggg classEggg = eggg.getClassEggg(typeEggg);
            assertNotNull(classEggg);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should complete quickly (adjust threshold as needed)
        assertTrue(duration < 1000, "Repeated access should be fast due to caching");
    }

    @Test
    void testMultipleGenericTypes() {
        TypeEggg typeEggg1 = eggg.getTypeEggg(List.class);
        TypeEggg typeEggg2 = eggg.getTypeEggg(Map.class);
        TypeEggg typeEggg3 = eggg.getTypeEggg(Set.class);

        assertNotNull(typeEggg1);
        assertNotNull(typeEggg2);
        assertNotNull(typeEggg3);

        assertTrue(typeEggg1.isList());
        assertTrue(typeEggg2.isMap());
        assertFalse(typeEggg3.isList());
        assertFalse(typeEggg3.isMap());
    }

    @Test
    void testErrorConditions() {
        // Test null safety
        assertThrows(NullPointerException.class, () -> eggg.getTypeEggg(null));

        // Test with invalid types (should not throw)
        TypeEggg typeEggg = eggg.getTypeEggg(Object.class);
        assertNotNull(typeEggg);
    }
}