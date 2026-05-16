package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassEggg 缺漏方法的补充测试
 * 覆盖: getGenericType, getElement, getAnnotations, findConstrEggg, findConstrEgggOrNull,
 *       findMethodEgggOrNull, getOwnMethodEgggs, getConstrEgggs(不可变性), hashCode
 */
class ClassWrapMoreTest {

    private final Eggg eggg = new Eggg();

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value() default "";
    }

    @TestAnnotation("hello")
    static class AnnotatedClass {
        private String name;
        private int age;

        public AnnotatedClass() {}

        public AnnotatedClass(String name) { this.name = name; }

        public AnnotatedClass(String name, int age) { this.name = name; this.age = age; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public void publicMethod() {}

        private void privateMethod() {}
    }

    static class GenericClass<T> {
        public T value;
        public GenericClass() {}
        public T getValue() { return value; }
    }

    // ======================== getGenericType ========================

    @Test
    void testGetGenericType_plainClass() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        Type genericType = cw.getGenericType();
        assertNotNull(genericType);
        assertEquals(AnnotatedClass.class, genericType);
    }

    @Test
    void testGetGenericType_matchesTypeEggg() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(GenericClass.class));
        assertEquals(cw.getTypeEggg().getGenericType(), cw.getGenericType());
    }

    // ======================== getElement ========================

    @Test
    void testGetElement_returnsClassObject() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        AnnotatedElement element = cw.getElement();
        assertNotNull(element);
        assertSame(AnnotatedClass.class, element);
    }

    @Test
    void testGetElement_canRetrieveAnnotations() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        AnnotatedElement element = cw.getElement();
        TestAnnotation anno = element.getAnnotation(TestAnnotation.class);
        assertNotNull(anno);
        assertEquals("hello", anno.value());
    }

    // ======================== getAnnotations ========================

    @Test
    void testGetAnnotations_annotatedClass() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        java.lang.annotation.Annotation[] annotations = cw.getAnnotations();
        assertNotNull(annotations);
        assertTrue(annotations.length > 0);

        boolean found = false;
        for (java.lang.annotation.Annotation a : annotations) {
            if (a instanceof TestAnnotation) {
                assertEquals("hello", ((TestAnnotation) a).value());
                found = true;
            }
        }
        assertTrue(found, "应包含 TestAnnotation");
    }

    @Test
    void testGetAnnotations_lazyInit() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        // 多次调用应返回同一数组引用（惰性缓存）
        java.lang.annotation.Annotation[] first = cw.getAnnotations();
        java.lang.annotation.Annotation[] second = cw.getAnnotations();
        assertSame(first, second, "getAnnotations 应该缓存结果");
    }

    @Test
    void testGetAnnotations_noAnnotations() {
        // 原始类型包装类如 int 没有自定义注解
        // 用一个无注解的内部类
        class PlainClass {}
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(PlainClass.class));
        java.lang.annotation.Annotation[] annotations = cw.getAnnotations();
        assertNotNull(annotations);
        // 内部类只有编译器生成的注解或没有
    }

    // ======================== findConstrEggg ========================

    @Test
    void testFindConstrEggg_noArg() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEggg();
        assertNotNull(c);
        assertEquals(0, c.getParamCount());
    }

    @Test
    void testFindConstrEggg_oneArg() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEggg(String.class);
        assertNotNull(c);
        assertEquals(1, c.getParamCount());
    }

    @Test
    void testFindConstrEggg_twoArgs() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEggg(String.class, int.class);
        assertNotNull(c);
        assertEquals(2, c.getParamCount());
    }

    @Test
    void testFindConstrEggg_notFound_throwsException() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        assertThrows(NoSuchMethodException.class, () -> {
            cw.findConstrEggg(Integer.class, Double.class, Long.class);
        });
    }

    // ======================== findConstrEgggOrNull ========================

    @Test
    void testFindConstrEgggOrNull_found() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEgggOrNull(String.class);
        assertNotNull(c);
        assertEquals(1, c.getParamCount());
    }

    @Test
    void testFindConstrEgggOrNull_notFound_returnsNull() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEgggOrNull(Integer.class, Double.class);
        assertNull(c);
    }

    @Test
    void testFindConstrEgggOrNull_noArg() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        ConstrEggg c = cw.findConstrEgggOrNull();
        assertNotNull(c);
        assertEquals(0, c.getParamCount());
    }

    // ======================== getConstrEgggs 不可变性 ========================

    @Test
    void testGetConstrEgggs_unmodifiable() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        List<ConstrEggg> list = cw.getConstrEgggs();
        assertNotNull(list);
        assertTrue(list.size() >= 3);

        assertThrows(UnsupportedOperationException.class, () -> {
            list.add(null);
        }, "getConstrEgggs 应返回不可变列表");
    }

    // ======================== findMethodEgggOrNull ========================

    @Test
    void testFindMethodEgggOrNull_publicMethod() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        MethodEggg m = cw.findMethodEgggOrNull("publicMethod");
        assertNotNull(m);
        assertEquals("publicMethod", m.getName());
        assertEquals(0, m.getParamCount());
    }

    @Test
    void testFindMethodEgggOrNull_methodWithParams() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        MethodEggg m = cw.findMethodEgggOrNull("setName", String.class);
        assertNotNull(m);
        assertEquals("setName", m.getName());
        assertEquals(1, m.getParamCount());
    }

    @Test
    void testFindMethodEgggOrNull_notFound_returnsNull() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        MethodEggg m = cw.findMethodEgggOrNull("nonExistentMethod");
        assertNull(m);
    }

    @Test
    void testFindMethodEgggOrNull_wrongParamTypes_returnsNull() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        MethodEggg m = cw.findMethodEgggOrNull("setName", Integer.class);
        assertNull(m);
    }

    // ======================== getOwnMethodEgggs ========================

    @Test
    void testGetOwnMethodEgggs_containsPublicAndNonPublic() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        Collection<MethodEggg> ownMethods = cw.getOwnMethodEgggs();
        assertNotNull(ownMethods);
        assertTrue(ownMethods.size() > 0);

        // 应包含 publicMethod
        boolean hasPublic = false;
        for (MethodEggg m : ownMethods) {
            if (m.getName().equals("publicMethod")) {
                hasPublic = true;
                break;
            }
        }
        assertTrue(hasPublic, "应包含 publicMethod");

        // 应包含 privateMethod
        boolean hasPrivate = false;
        for (MethodEggg m : ownMethods) {
            if (m.getName().equals("privateMethod")) {
                hasPrivate = true;
                break;
            }
        }
        assertTrue(hasPrivate, "应包含 privateMethod");
    }

    @Test
    void testGetOwnMethodEgggs_excludesObjectMethods() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        Collection<MethodEggg> ownMethods = cw.getOwnMethodEgggs();

        for (MethodEggg m : ownMethods) {
            assertNotEquals("hashCode", m.getName(), "不应包含 Object.hashCode");
            assertNotEquals("toString", m.getName(), "不应包含 Object.toString");
            assertNotEquals("equals", m.getName(), "不应包含 Object.equals");
        }
    }

    // ======================== hashCode ========================

    @Test
    void testHashCode_sameTypeEggg_sameValue() {
        TypeEggg typeEggg = eggg.getTypeEggg(AnnotatedClass.class);
        ClassEggg cw1 = eggg.getClassEggg(typeEggg);
        ClassEggg cw2 = eggg.getClassEggg(typeEggg);

        // 同一个缓存实例
        assertSame(cw1, cw2);
        assertEquals(cw1.hashCode(), cw2.hashCode());
    }

    @Test
    void testHashCode_equalsTypeEgggHashCode() {
        TypeEggg typeEggg = eggg.getTypeEggg(AnnotatedClass.class);
        ClassEggg cw = eggg.getClassEggg(typeEggg);
        assertEquals(typeEggg.hashCode(), cw.hashCode(), "hashCode 应委托给 typeEggg");
    }

    // ======================== findMethodEggg 异常路径 ========================

    @Test
    void testFindMethodEggg_notFound_throwsException() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        assertThrows(NoSuchMethodException.class, () -> {
            cw.findMethodEggg("nonExistentMethod");
        });
    }

    @Test
    void testFindMethodEggg_found() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        MethodEggg m = cw.findMethodEggg("getName");
        assertNotNull(m);
        assertEquals("getName", m.getName());
    }

    // ======================== findMethodEgggOrNew ========================

    @Test
    void testFindMethodEgggOrNew_existingMethod() throws Exception {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(AnnotatedClass.class));
        Method getNameMethod = AnnotatedClass.class.getMethod("getName");

        MethodEggg m1 = cw.findMethodEgggOrNew(getNameMethod);
        MethodEggg m2 = cw.findMethodEgggOrNew(getNameMethod);
        assertNotNull(m1);
        assertSame(m1, m2, "对同一 Method 应返回缓存的同一 MethodEggg");
    }
}
