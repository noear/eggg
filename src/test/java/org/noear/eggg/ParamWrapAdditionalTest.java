package org.noear.eggg;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

class ParamWrapAdditionalTest {

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

        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);
        assertNotNull(methodWrap);

        java.util.List<ParamEggg> params = methodWrap.getParamWrapAry();
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
        ParamEggg paramWrap = eggg.newParamWrap(classEggg, parameter);

        assertNotNull(paramWrap);
        assertEquals("stringParam", paramWrap.getName());
        assertEquals(String.class, paramWrap.getTypeEggg().getType());
        assertNotNull(paramWrap.getParam());

        // Test alias (should be null without alias handler)
        assertNotNull(paramWrap.getAlias());

        // Test digest (should be null without digest handler)
        assertNull(paramWrap.getDigest());
    }

    @Test
    void testFinalParameter() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithFinalParam", String.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramWrap = eggg.newParamWrap(classEggg, parameter);

        assertNotNull(paramWrap);
        assertEquals("finalParam", paramWrap.getName());
    }

    @Test
    void testAnnotatedParameter() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithAnnotatedParam", String.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramWrap = eggg.newParamWrap(classEggg, parameter);

        assertNotNull(paramWrap);
        assertEquals("annotatedParam", paramWrap.getName());

        // The parameter should have the annotation
        Parameter originalParam = paramWrap.getParam();
        assertTrue(originalParam.isAnnotationPresent(Deprecated.class));
    }

    @Test
    void testParameterWithCustomHandlers() throws Exception {
        // Test with custom digest and alias handlers
        DigestHandler<String> digestHandler = (cw, holder, source, def) -> "param_digest";
        AliasHandler<String> aliasHandler = (cw, holder, digest, def) -> "custom_" + digest;

        Eggg customEggg = new Eggg()
                .withDigestHandler(digestHandler)
                .withAliasHandler(aliasHandler);

        ClassEggg classEggg = customEggg.getClassEggg(customEggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams", String.class, int.class, Integer.class, String[].class, java.util.List.class);

        Parameter parameter = method.getParameters()[0];
        ParamEggg paramWrap = customEggg.newParamWrap(classEggg, parameter);

        assertNotNull(paramWrap);
        assertEquals("param_digest", paramWrap.getDigest());
        assertEquals("custom_param_digest", paramWrap.getAlias());
    }

    @Test
    void testParameterTypeEggg() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(ClassWithVariousParameters.class));
        Method method = ClassWithVariousParameters.class.getMethod("methodWithParams", String.class, int.class, Integer.class, String[].class, java.util.List.class);

        Parameter parameter = method.getParameters()[4]; // List parameter
        ParamEggg paramWrap = eggg.newParamWrap(classEggg, parameter);

        assertNotNull(paramWrap);
        TypeEggg typeEggg = paramWrap.getTypeEggg();
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
        ConstrEggg constrWrap = classEggg.getConstrWrap();

        assertNotNull(constrWrap);
        assertEquals(1, constrWrap.getParamCount());

        ParamEggg paramWrap = constrWrap.getParamWrapAry().get(0);
        assertNotNull(paramWrap);
        assertEquals("value", paramWrap.getName());
        assertEquals(String.class, paramWrap.getTypeEggg().getType());
    }

    @Test
    void testGenericParameterType() throws Exception {
        class GenericClass<T> {
            public void process(T item) {}
        }

        class StringClass extends GenericClass<String> {}

        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringClass.class));
        Method method = GenericClass.class.getMethod("process", Object.class);

        MethodEggg methodWrap = eggg.newMethodWrap(classEggg, method);
        assertNotNull(methodWrap);

        ParamEggg paramWrap = methodWrap.getParamWrapAry().get(0);
        assertNotNull(paramWrap);

        // The parameter type should be resolved to String due to generic inheritance
        TypeEggg typeEggg = paramWrap.getTypeEggg();
        assertNotNull(typeEggg);
        assertEquals(String.class, typeEggg.getType());
    }
}