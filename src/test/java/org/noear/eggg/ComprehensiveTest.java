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
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ConcreteClass.class));

        // Test fields
        FieldWrap genericField = classWrap.getFieldWrapByName("genericField");
        assertNotNull(genericField);
        assertEquals(String.class, genericField.getTypeWrap().getType());

        FieldWrap numberListField = classWrap.getFieldWrapByName("numberList");
        assertNotNull(numberListField);
        assertTrue(numberListField.getTypeWrap().isList());

        // Test properties
        PropertyWrap genericProperty = classWrap.getPropertyWrapByName("genericField");
        assertNotNull(genericProperty);
        assertNotNull(genericProperty.getGetterWrap());
        assertNotNull(genericProperty.getSetterWrap());
    }

    @Test
    void testGenericMethodInComplexClass() {
        ClassWrap classWrap = eggg.getClassWrap(eggg.getTypeWrap(ComplexGenericClass.class));

        Optional<MethodWrap> processMethod = classWrap.getPublicMethodWraps().stream()
                .filter(m -> m.getName().equals("processData"))
                .findFirst();

        assertTrue(processMethod.isPresent());
        MethodWrap methodWrap = processMethod.get();
        assertEquals(3, methodWrap.getParamCount());

        List<ParamWrap> params = methodWrap.getParamWrapAry();
        assertNotNull(params);
        assertEquals(3, params.size());
    }

    @Test
    void testPerformanceWithCaching() {
        long startTime = System.currentTimeMillis();

        // Repeated access should be fast due to caching
        for (int i = 0; i < 100; i++) {
            TypeWrap typeWrap = eggg.getTypeWrap(String.class);
            ClassWrap classWrap = eggg.getClassWrap(typeWrap);
            assertNotNull(classWrap);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should complete quickly (adjust threshold as needed)
        assertTrue(duration < 1000, "Repeated access should be fast due to caching");
    }

    @Test
    void testMultipleGenericTypes() {
        TypeWrap typeWrap1 = eggg.getTypeWrap(List.class);
        TypeWrap typeWrap2 = eggg.getTypeWrap(Map.class);
        TypeWrap typeWrap3 = eggg.getTypeWrap(Set.class);

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
        TypeWrap typeWrap = eggg.getTypeWrap(Object.class);
        assertNotNull(typeWrap);
    }
}