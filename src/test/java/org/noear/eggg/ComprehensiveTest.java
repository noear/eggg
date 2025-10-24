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
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(ConcreteClass.class));

        // Test fields
        FieldEggg genericField = classWrap.getFieldWrapByName("genericField");
        assertNotNull(genericField);
        assertEquals(String.class, genericField.getTypeWrap().getType());

        FieldEggg numberListField = classWrap.getFieldWrapByName("numberList");
        assertNotNull(numberListField);
        assertTrue(numberListField.getTypeWrap().isList());

        // Test properties
        PropertyEggg genericProperty = classWrap.getPropertyWrapByName("genericField");
        assertNotNull(genericProperty);
        assertNotNull(genericProperty.getGetterWrap());
        assertNotNull(genericProperty.getSetterWrap());
    }

    @Test
    void testGenericMethodInComplexClass() {
        ClassEggg classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexGenericClass.class));

        Optional<MethodEggg> processMethod = classWrap.getPublicMethodWraps().stream()
                .filter(m -> m.getName().equals("processData"))
                .findFirst();

        assertTrue(processMethod.isPresent());
        MethodEggg methodWrap = processMethod.get();
        assertEquals(3, methodWrap.getParamCount());

        List<ParamEggg> params = methodWrap.getParamWrapAry();
        assertNotNull(params);
        assertEquals(3, params.size());
    }

    @Test
    void testPerformanceWithCaching() {
        long startTime = System.currentTimeMillis();

        // Repeated access should be fast due to caching
        for (int i = 0; i < 100; i++) {
            TypeEggg typeWrap = eggg.getTypeWrap(String.class);
            ClassEggg classWrap = eggg.getClassWrap(typeWrap);
            assertNotNull(classWrap);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should complete quickly (adjust threshold as needed)
        assertTrue(duration < 1000, "Repeated access should be fast due to caching");
    }

    @Test
    void testMultipleGenericTypes() {
        TypeEggg typeWrap1 = eggg.getTypeWrap(List.class);
        TypeEggg typeWrap2 = eggg.getTypeWrap(Map.class);
        TypeEggg typeWrap3 = eggg.getTypeWrap(Set.class);

        assertNotNull(typeWrap1);
        assertNotNull(typeWrap2);
        assertNotNull(typeWrap3);

        assertTrue(typeWrap1.isList());
        assertTrue(typeWrap2.isMap());
        assertFalse(typeWrap3.isList());
        assertFalse(typeWrap3.isMap());
    }

    @Test
    void testErrorConditions() {
        // Test null safety
        assertThrows(NullPointerException.class, () -> eggg.getTypeWrap(null));

        // Test with invalid types (should not throw)
        TypeEggg typeWrap = eggg.getTypeWrap(Object.class);
        assertNotNull(typeWrap);
    }
}