package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

class ParamEgggAdditionalTest {

    private final Eggg eggg = new Eggg();

    static class ClassWithVariousParameters {
        public void methodWithParams(
                String stringParam,
                int intParam,
                Integer integerParam,
                String[] arrayParam,
                java.util.List<String> listParam
        ) {}

        public void methodWithFinalParam(final String finalParam) {}
        public void methodWithAnnotatedParam(@Deprecated String annotatedParam) {}
    }

    @Test
    void testVariousParameterTypes() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams",
                String.class, int.class, Integer.class, String[].class, java.util.List.class);

        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);
        assertNotNull(methodEggg);

        java.util.List<ParamEggg> params = methodEggg.getParamEgggAry();
        assertEquals(5, params.size());

        // Test different parameter types
        assertEquals(String.class, params.get(0).getTypeEggg().getType());
        assertEquals(int.class, params.get(1).getTypeEggg().getType());
        assertEquals(Integer.class, params.get(2).getTypeEggg().getType());
        assertEquals(String[].class, params.get(3).getTypeEggg().getType());
        assertTrue(params.get(4).getTypeEggg().isList());
    }

    @Test
    void testParameterProperties() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams",
                String.class, int.class, Integer.class, String[].class, java.util.List.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramEggg = eggg.newParamEggg(classEggg, parameter);

        assertNotNull(paramEggg);
        assertEquals("stringParam", paramEggg.getName());
        assertEquals(String.class, paramEggg.getTypeEggg().getType());
        assertNotNull(paramEggg.getParam());

        // Test alias (should be null without alias handler)
        assertNotNull(paramEggg.getAlias());

        // Test digest (should be null without digest handler)
        assertNull(paramEggg.getDigest());
    }

    @Test
    void testFinalParameter() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithFinalParam", String.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramEggg = eggg.newParamEggg(classEggg, parameter);

        assertNotNull(paramEggg);
        assertEquals("finalParam", paramEggg.getName());
    }

    @Test
    void testAnnotatedParameter() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithAnnotatedParam", String.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramEggg = eggg.newParamEggg(classEggg, parameter);

        assertNotNull(paramEggg);
        assertEquals("annotatedParam", paramEggg.getName());

        // The parameter should have the annotation
        Parameter originalParam = paramEggg.getParam();
        assertTrue(originalParam.isAnnotationPresent(Deprecated.class));
    }

    @Test
    void testParameterWithCustomHandlers() throws Exception {
        // Test with custom digest and alias handlers
        DigestHandler digestHandler = (cw, s, def) -> "param_digest";
        AliasHandler aliasHandler = (cw, s, def) -> "custom_" + s.getDigest();

        Eggg customEggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams", String.class, int.class, Integer.class, String[].class, java.util.List.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramEggg = customEggg.newParamEggg(classEggg, parameter);

        assertNotNull(paramEggg);
        assertEquals("param_digest", paramEggg.getDigest());
        assertEquals("custom_param_digest", paramEggg.getAlias());
    }

    @Test
    void testParameterTypeEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams", String.class, int.class, Integer.class, String[].class, java.util.List.class);

        Parameter parameter = method.getParameters()[4]; // List parameter
        ParamEggg paramEggg = eggg.newParamEggg(classEggg, parameter);

        assertNotNull(paramEggg);
        TypeEggg typeEggg = paramEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertTrue(typeEggg.isList());
        assertTrue(typeEggg.isParameterizedType());
    }

    static class ClassWithParameterizedConstructor {
        private String value;

        public ClassWithParameterizedConstructor(String value) {
            this.value = value;
        }
    }

    @Test
    void testParameterInConstructor() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithParameterizedConstructor.class));
        ConstrEggg constrEggg = classEggg.getCreator();

        assertNotNull(constrEggg);
        assertEquals(1, constrEggg.getParamCount());

        ParamEggg paramEggg = constrEggg.getParamEgggAry().get(0);
        assertNotNull(paramEggg);
        assertEquals("value", paramEggg.getName());
        assertEquals(String.class, paramEggg.getTypeEggg().getType());
    }

    @Test
    void testGenericParameterType() throws Exception {
        class GenericClass<T> {
            public void process(T item) {}
        }

        class StringClass extends GenericClass<String> {}

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringClass.class));
        Method method = GenericClass.class.getMethod("process", Object.class);

        MethodEggg methodEggg = eggg.newMethodEggg(classEggg, method);
        assertNotNull(methodEggg);

        ParamEggg paramEggg = methodEggg.getParamEgggAry().get(0);
        assertNotNull(paramEggg);

        // The parameter type should be resolved to String due to generic inheritance
        TypeEggg typeEggg = paramEggg.getTypeEggg();
        assertNotNull(typeEggg);
        assertEquals(String.class, typeEggg.getType());
    }
}