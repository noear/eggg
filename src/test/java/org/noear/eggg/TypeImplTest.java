package org.noear.eggg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Type 实现类单元测试
 */
class TypeImplTest {

    // ==================== ParameterizedTypeImpl 测试 ====================

    @Test
    @DisplayName("ParameterizedTypeImpl - 基本功能测试")
    void testParameterizedTypeImplBasic() throws NoSuchMethodException {
        // 获取 JDK 的 ParameterizedType 作为参考
        Method method = TypeImplTest.class.getDeclaredMethod("getParameterizedType");
        Type jdkType = method.getGenericReturnType();
        ParameterizedType jdkParamType = (ParameterizedType) jdkType;

        // 创建自定义实现
        GenericResolver.ParameterizedTypeImpl customType = new GenericResolver.ParameterizedTypeImpl(
                (Class<?>) jdkParamType.getRawType(),
                jdkParamType.getActualTypeArguments(),
                jdkParamType.getOwnerType()
        );

        // 基本功能测试
        assertNotNull(customType.getRawType());
        assertNotNull(customType.getActualTypeArguments());
        assertEquals(jdkParamType.getActualTypeArguments().length,
                customType.getActualTypeArguments().length);
    }

    @Test
    @DisplayName("ParameterizedTypeImpl - equals 和 hashCode 一致性测试")
    void testParameterizedTypeImplEqualsHashCode() throws NoSuchMethodException {
        Method method = TypeImplTest.class.getDeclaredMethod("getParameterizedType");
        ParameterizedType jdkType = (ParameterizedType) method.getGenericReturnType();

        // 创建两个相同的自定义实例
        GenericResolver.ParameterizedTypeImpl impl1 = new GenericResolver.ParameterizedTypeImpl(
                (Class<?>) jdkType.getRawType(),
                jdkType.getActualTypeArguments(),
                jdkType.getOwnerType()
        );

        GenericResolver.ParameterizedTypeImpl impl2 = new GenericResolver.ParameterizedTypeImpl(
                (Class<?>) jdkType.getRawType(),
                jdkType.getActualTypeArguments(),
                jdkType.getOwnerType()
        );

        // equals 测试
        assertEquals(impl1, impl2);
        assertEquals(impl2, impl1);
        assertNotEquals(impl1, null);
        assertNotEquals(impl1, "string");
        assertEquals(impl1, impl1); // 自反性

        // hashCode 一致性测试
        assertEquals(impl1.hashCode(), impl2.hashCode());

        // 与 JDK 实现对比（虽然类不同，但可以对比模式）
        testHashCodePattern(jdkType, impl1);
    }

    @Test
    @DisplayName("ParameterizedTypeImpl - hashCode 模式对比 JDK")
    void testParameterizedTypeImplHashCodePattern() throws NoSuchMethodException {
        // 测试多个不同的 ParameterizedType
        testHashCodeForMethod("getParameterizedType");
        testHashCodeForMethod("getMapType");
        testHashCodeForMethod("getNestedType");
    }

    @Test
    @DisplayName("ParameterizedTypeImpl - toString 测试")
    void testParameterizedTypeImplToString() {
        GenericResolver.ParameterizedTypeImpl type = new GenericResolver.ParameterizedTypeImpl(
                List.class,
                new Type[]{String.class},
                null
        );

        String str = type.toString();
        assertTrue(str.contains("List"));
        assertTrue(str.contains("String"));
        assertTrue(str.contains("<") && str.contains(">"));
    }

    @Test
    @DisplayName("ParameterizedTypeImpl - 参数数量不匹配测试")
    void testParameterizedTypeImplArgumentMismatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            new GenericResolver.ParameterizedTypeImpl(
                    List.class, // 需要 1 个类型参数
                    new Type[]{String.class, Integer.class}, // 提供了 2 个
                    null
            );
        });
    }

    // ==================== GenericArrayTypeImpl 测试 ====================

    @Test
    @DisplayName("GenericArrayTypeImpl - 基本功能测试")
    void testGenericArrayTypeImplBasic() throws NoSuchMethodException {
        Method method = TypeImplTest.class.getDeclaredMethod("getGenericArray");
        GenericArrayType jdkType = (GenericArrayType) method.getGenericReturnType();

        GenericResolver.GenericArrayTypeImpl customType = new GenericResolver.GenericArrayTypeImpl(
                jdkType.getGenericComponentType()
        );

        assertNotNull(customType.getGenericComponentType());
        assertEquals(jdkType.getGenericComponentType(),
                customType.getGenericComponentType());
    }

    @Test
    @DisplayName("GenericArrayTypeImpl - equals 和 hashCode 测试")
    void testGenericArrayTypeImplEqualsHashCode() throws NoSuchMethodException {
        Method method = TypeImplTest.class.getDeclaredMethod("getGenericArray");
        GenericArrayType jdkType = (GenericArrayType) method.getGenericReturnType();

        GenericResolver.GenericArrayTypeImpl impl1 = new GenericResolver.GenericArrayTypeImpl(
                jdkType.getGenericComponentType()
        );

        GenericResolver.GenericArrayTypeImpl impl2 = new GenericResolver.GenericArrayTypeImpl(
                jdkType.getGenericComponentType()
        );

        // equals 测试
        assertEquals(impl1, impl2);
        assertNotEquals(impl1, null);
        assertNotEquals(impl1, new Object());

        // hashCode 一致性测试
        assertEquals(impl1.hashCode(), impl2.hashCode());

        // 与 JDK 实现对比
        testHashCodePattern(jdkType, impl1);
    }

    @Test
    @DisplayName("GenericArrayTypeImpl - toString 测试")
    void testGenericArrayTypeImplToString() throws NoSuchMethodException {
        Method method = TypeImplTest.class.getDeclaredMethod("getGenericArray");
        GenericArrayType jdkType = (GenericArrayType) method.getGenericReturnType();

        GenericResolver.GenericArrayTypeImpl impl = new GenericResolver.GenericArrayTypeImpl(
                jdkType.getGenericComponentType()
        );

        String str = impl.toString();
        assertTrue(str.endsWith("[]"));
        assertTrue(str.contains("List"));
    }

    // ==================== WildcardTypeImpl 测试 ====================

    @Test
    @DisplayName("WildcardTypeImpl - 上界通配符测试")
    void testWildcardTypeImplUpperBound() {
        // 直接创建 WildcardType，不通过反射获取
        GenericResolver.WildcardTypeImpl customType = new GenericResolver.WildcardTypeImpl(
                new Type[]{Number.class}, // 上界
                new Type[0]              // 下界为空
        );

        assertEquals(1, customType.getUpperBounds().length);
        assertEquals(0, customType.getLowerBounds().length);
        assertEquals(Number.class, customType.getUpperBounds()[0]);
    }

    @Test
    @DisplayName("WildcardTypeImpl - 下界通配符测试")
    void testWildcardTypeImplLowerBound() {
        GenericResolver.WildcardTypeImpl customType = new GenericResolver.WildcardTypeImpl(
                new Type[]{Object.class}, // 上界为 Object
                new Type[]{String.class}  // 下界
        );

        assertEquals(1, customType.getLowerBounds().length);
        assertEquals(1, customType.getUpperBounds().length);
        assertEquals(String.class, customType.getLowerBounds()[0]);
        assertEquals(Object.class, customType.getUpperBounds()[0]);
    }

    @Test
    @DisplayName("WildcardTypeImpl - 无界通配符测试")
    void testWildcardTypeImplUnbounded() {
        GenericResolver.WildcardTypeImpl customType = new GenericResolver.WildcardTypeImpl(
                new Type[]{Object.class}, // 上界
                new Type[0]              // 下界为空
        );

        // 无界通配符应该有一个 Object 的上界
        assertEquals(1, customType.getUpperBounds().length);
        assertEquals(Object.class, customType.getUpperBounds()[0]);
        assertEquals(0, customType.getLowerBounds().length);
    }

    @Test
    @DisplayName("WildcardTypeImpl - equals 和 hashCode 测试")
    void testWildcardTypeImplEqualsHashCode() {
        // 创建两个相同的 WildcardType 实例
        GenericResolver.WildcardTypeImpl impl1 = new GenericResolver.WildcardTypeImpl(
                new Type[]{Number.class},
                new Type[0]
        );

        GenericResolver.WildcardTypeImpl impl2 = new GenericResolver.WildcardTypeImpl(
                new Type[]{Number.class},
                new Type[0]
        );

        // equals 测试
        assertEquals(impl1, impl2);
        assertEquals(impl2, impl1);
        assertNotEquals(impl1, null);
        assertNotEquals(impl1, "string");
        assertEquals(impl1, impl1); // 自反性

        // hashCode 一致性测试
        assertEquals(impl1.hashCode(), impl2.hashCode());

        // 测试不同内容的实例
        GenericResolver.WildcardTypeImpl different = new GenericResolver.WildcardTypeImpl(
                new Type[]{String.class},
                new Type[0]
        );
        assertNotEquals(impl1, different);
        assertNotEquals(impl1.hashCode(), different.hashCode());
    }

    @Test
    @DisplayName("WildcardTypeImpl - 从 ParameterizedType 提取 WildcardType 测试")
    void testWildcardTypeFromParameterizedType() throws NoSuchMethodException {
        // 获取包含通配符的 ParameterizedType
        Method method = TypeImplTest.class.getDeclaredMethod("getUpperBoundWildcard");
        ParameterizedType paramType = (ParameterizedType) method.getGenericReturnType();

        // 提取实际的 WildcardType
        Type[] typeArgs = paramType.getActualTypeArguments();
        assertEquals(1, typeArgs.length);
        assertTrue(typeArgs[0] instanceof WildcardType);

        WildcardType jdkWildcard = (WildcardType) typeArgs[0];

        // 创建对应的自定义实现
        GenericResolver.WildcardTypeImpl customWildcard = new GenericResolver.WildcardTypeImpl(
                jdkWildcard.getUpperBounds(),
                jdkWildcard.getLowerBounds()
        );

        // 验证内容一致
        assertEquals(jdkWildcard.getUpperBounds().length, customWildcard.getUpperBounds().length);
        assertEquals(jdkWildcard.getLowerBounds().length, customWildcard.getLowerBounds().length);

        // 测试 hashCode 模式
        testHashCodePattern(jdkWildcard, customWildcard);
    }

    @Test
    @DisplayName("WildcardTypeImpl - 非法边界组合测试")
    void testWildcardTypeImplInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> {
            new GenericResolver.WildcardTypeImpl(
                    new Type[]{Number.class}, // 上界
                    new Type[]{String.class}  // 下界 - 不应该同时存在非Object上界和下界
            );
        });
    }

    @Test
    @DisplayName("WildcardTypeImpl - toString 测试")
    void testWildcardTypeImplToString() {
        GenericResolver.WildcardTypeImpl upper = new GenericResolver.WildcardTypeImpl(new Type[]{Number.class}, new Type[0]);
        assertTrue(upper.toString().contains("extends"));
        assertTrue(upper.toString().contains("Number"));

        GenericResolver.WildcardTypeImpl lower = new GenericResolver.WildcardTypeImpl(new Type[]{Object.class}, new Type[]{String.class});
        assertTrue(lower.toString().contains("super"));
        assertTrue(lower.toString().contains("String"));

        GenericResolver.WildcardTypeImpl unbounded = new GenericResolver.WildcardTypeImpl(new Type[]{Object.class}, new Type[0]);
        assertEquals("?", unbounded.toString());
    }

    @Test
    @DisplayName("WildcardTypeImpl - 多上界测试")
    void testWildcardTypeImplMultipleUpperBounds() {
        // 测试多个上界的情况（如 <T extends Number & Comparable>）
        GenericResolver.WildcardTypeImpl multiUpper = new GenericResolver.WildcardTypeImpl(
                new Type[]{Number.class, Comparable.class},
                new Type[0]
        );

        assertEquals(2, multiUpper.getUpperBounds().length);
        assertEquals(Number.class, multiUpper.getUpperBounds()[0]);
        assertEquals(Comparable.class, multiUpper.getUpperBounds()[1]);

        String toString = multiUpper.toString();
        assertTrue(toString.contains("extends"));
        assertTrue(toString.contains("Number"));
        assertTrue(toString.contains("Comparable"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 测试 hashCode 模式是否与 JDK 实现一致
     */
    private void testHashCodePattern(Type jdkType, Type customType) {
        // JDK 实现的 hashCode 应该是一致的（多次调用返回相同值）
        int jdkHash1 = jdkType.hashCode();
        int jdkHash2 = jdkType.hashCode();
        assertEquals(jdkHash1, jdkHash2);

        // 自定义实现的 hashCode 也应该是一致的
        int customHash1 = customType.hashCode();
        int customHash2 = customType.hashCode();
        assertEquals(customHash1, customHash2);

        // 虽然 JDK 和自定义实现的 hashCode 值可能不同（因为类不同），
        // 但我们应该确保自定义实现的 hashCode 分布合理
        assertTrue(customHash1 != 0 || customType instanceof WildcardType);

        // 测试相同内容的不同实例应该有相同的 hashCode
        if (customType instanceof GenericResolver.ParameterizedTypeImpl) {
            GenericResolver.ParameterizedTypeImpl pt1 = (GenericResolver.ParameterizedTypeImpl) customType;
            GenericResolver.ParameterizedTypeImpl pt2 = new GenericResolver.ParameterizedTypeImpl(
                    (Class<?>) pt1.getRawType(),
                    pt1.getActualTypeArguments(),
                    pt1.getOwnerType()
            );
            assertEquals(pt1.hashCode(), pt2.hashCode());
        } else if (customType instanceof GenericResolver.WildcardTypeImpl) {
            GenericResolver.WildcardTypeImpl wt1 = (GenericResolver.WildcardTypeImpl) customType;
            GenericResolver.WildcardTypeImpl wt2 = new GenericResolver.WildcardTypeImpl(
                    wt1.getUpperBounds(),
                    wt1.getLowerBounds()
            );
            assertEquals(wt1.hashCode(), wt2.hashCode());
        }
    }

    private void testHashCodeForMethod(String methodName) throws NoSuchMethodException {
        Method method = TypeImplTest.class.getDeclaredMethod(methodName);
        Type jdkType = method.getGenericReturnType();

        if (jdkType instanceof ParameterizedType) {
            ParameterizedType jdkParamType = (ParameterizedType) jdkType;
            GenericResolver.ParameterizedTypeImpl customType = new GenericResolver.ParameterizedTypeImpl(
                    (Class<?>) jdkParamType.getRawType(),
                    jdkParamType.getActualTypeArguments(),
                    jdkParamType.getOwnerType()
            );
            testHashCodePattern(jdkParamType, customType);
        }
    }

    // ==================== 测试用的泛型方法 ====================

    private static List<String> getParameterizedType() {
        return null;
    }

    private static Map<String, Integer> getMapType() {
        return null;
    }

    private static List<Map<String, List<Integer>>> getNestedType() {
        return null;
    }

    private static List<String>[] getGenericArray() {
        return null;
    }

    private static List<? extends Number> getUpperBoundWildcard() {
        return null;
    }

    private static List<? super String> getLowerBoundWildcard() {
        return null;
    }

    private static List<?> getUnboundedWildcard() {
        return null;
    }

    // 用于测试多上界的辅助方法
    private static <T extends Number & Comparable> List<T> getMultipleUpperBounds() {
        return null;
    }
}
