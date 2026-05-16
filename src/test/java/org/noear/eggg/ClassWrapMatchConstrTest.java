package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassEggg.matchConstrEggg 方法的单元测试
 */
class ClassWrapMatchConstrTest {

    private final Eggg eggg = new Eggg();

    /**
     * 多构造器类：无参、双参、三参
     * 参数名在编译后作为 alias（无 aliasHandler 时 alias == name）
     */
    static class MultiConstrClass {
        public MultiConstrClass() {}

        public MultiConstrClass(String name, int age) {}

        public MultiConstrClass(String name, int age, String city) {}
    }

    /**
     * 单构造器类
     */
    static class SingleConstrClass {
        public SingleConstrClass(String value) {}
    }

    /**
     * 仅有有参构造器的类
     */
    static class OnlyParamConstrClass {
        public OnlyParamConstrClass(String a) {}
    }

    /**
     * 两个有参构造器 + 一个无参构造器
     */
    static class ThreeConstrClass {
        public ThreeConstrClass() {}

        public ThreeConstrClass(String x) {}

        public ThreeConstrClass(String x, int y) {}
    }

    private Set<String> keysOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    // ======================== 快速路径测试 ========================

    @Test
    void testNullKeys_returnsDefConstr() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));
        ConstrEggg defConstr = cw.getCreator();

        ConstrEggg result = cw.matchConstrEggg(null, defConstr);
        assertSame(defConstr, result);
    }

    @Test
    void testEmptyKeys_returnsDefConstr() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));
        ConstrEggg defConstr = cw.getCreator();

        ConstrEggg result = cw.matchConstrEggg(Collections.emptySet(), defConstr);
        assertSame(defConstr, result);
    }

    @Test
    void testSingleConstructor_returnsDefConstr() {
        // 快速路径1：只有一个构造器
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(SingleConstrClass.class));
        ConstrEggg defConstr = cw.getCreator();

        Set<String> keys = keysOf("value");
        ConstrEggg result = cw.matchConstrEggg(keys, defConstr);
        assertSame(defConstr, result);
    }

    @Test
    void testNoArgDefConstr_returnsDefConstr() {
        // 快速路径2：默认构造器是无参的
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));
        ConstrEggg defConstr = cw.getCreator();

        assertEquals(0, defConstr.getParamCount(), "默认构造器应为无参");

        Set<String> keys = keysOf("name", "age");
        ConstrEggg result = cw.matchConstrEggg(keys, defConstr);
        assertSame(defConstr, result);
    }

    @Test
    void testOnlyOneParamConstructor_returnsDefConstr() {
        // 快速路径3：只有一个有参构造器
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(OnlyParamConstrClass.class));
        ConstrEggg defConstr = cw.getCreator();

        // OnlyParamConstrClass 只有 1 个有参构造器，所以 paramConstructorCount <= 1
        Set<String> keys = keysOf("a");
        ConstrEggg result = cw.matchConstrEggg(keys, defConstr);
        assertSame(defConstr, result);
    }

    // ======================== 完整匹配路径测试 ========================

    @Test
    void testFullMatch_selectsMoreParamsConstr() {
        // ThreeConstrClass: 无参(), String(x), String+int(x,y)
        // 用 ThreeConstrClass(String x) 作为 defConstr，keys 包含 x,y → 应匹配到 2 参数构造器
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(ThreeConstrClass.class));

        ConstrEggg oneParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 1) {
                oneParamConstr = c;
                break;
            }
        }
        assertNotNull(oneParamConstr);

        // 用 oneParamConstr 作为 defConstr，keys 包含 x,y → 应匹配到 2 参数构造器
        Set<String> keys = keysOf("x", "y");
        ConstrEggg result = cw.matchConstrEggg(keys, oneParamConstr);
        assertEquals(2, result.getParamCount(), "应匹配到参数最多的完全匹配构造器");
    }

    @Test
    void testFullMatch_partialMatch_returnsDefConstr() {
        // keys 只包含部分参数，不满足完全匹配
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(ThreeConstrClass.class));

        ConstrEggg oneParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 1) {
                oneParamConstr = c;
                break;
            }
        }
        assertNotNull(oneParamConstr);

        // keys 只包含 x，不包含 y → 2参数构造器不能完全匹配
        Set<String> keys = keysOf("x");
        ConstrEggg result = cw.matchConstrEggg(keys, oneParamConstr);
        assertSame(oneParamConstr, result, "部分匹配应返回 defConstr");
    }

    @Test
    void testFullMatch_exactMatchOneParam() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(ThreeConstrClass.class));

        ConstrEggg oneParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 1) {
                oneParamConstr = c;
                break;
            }
        }
        assertNotNull(oneParamConstr);

        // keys 包含 x → 精确匹配 oneParamConstr 本身（但 paramCount 不大于 defConstr，所以 break）
        Set<String> keys = keysOf("x");
        ConstrEggg result = cw.matchConstrEggg(keys, oneParamConstr);
        // candidate paramCount (1) <= defConstr.getParamCount() (1), 直接 break → 返回 defConstr
        assertSame(oneParamConstr, result);
    }

    @Test
    void testFullMatch_noMatchingKeys_returnsDefConstr() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(ThreeConstrClass.class));

        ConstrEggg oneParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 1) {
                oneParamConstr = c;
                break;
            }
        }
        assertNotNull(oneParamConstr);

        // keys 不包含任何构造器参数名
        Set<String> keys = keysOf("unknown", "missing");
        ConstrEggg result = cw.matchConstrEggg(keys, oneParamConstr);
        assertSame(oneParamConstr, result, "无匹配 keys 应返回 defConstr");
    }

    // ======================== 降序排序 + 首个全匹配即最优 ========================

    @Test
    void testDescendingOrder_firstFullMatchIsBest() {
        // MultiConstrClass: (), (String name, int age), (String name, int age, String city)
        // constrEgggs 已按参数数降序排列
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));

        // 找到 2 参数构造器作为 defConstr
        ConstrEggg twoParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 2) {
                twoParamConstr = c;
                break;
            }
        }
        assertNotNull(twoParamConstr);

        // keys 包含 name, age, city → 应匹配到 3 参数构造器（比 defConstr 参数更多）
        Set<String> keys = keysOf("name", "age", "city");
        ConstrEggg result = cw.matchConstrEggg(keys, twoParamConstr);
        assertEquals(3, result.getParamCount(), "应选择参数最多的完全匹配构造器");
    }

    @Test
    void testDescendingOrder_threeParamsButOnlyTwoMatch_returnsDefConstr() {
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));

        ConstrEggg twoParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 2) {
                twoParamConstr = c;
                break;
            }
        }
        assertNotNull(twoParamConstr);

        // keys 包含 name, age 但不含 city → 3 参数构造器不完全匹配
        // 2 参数构造器 paramCount == defConstr.getParamCount()，不会优于 defConstr
        Set<String> keys = keysOf("name", "age");
        ConstrEggg result = cw.matchConstrEggg(keys, twoParamConstr);
        assertSame(twoParamConstr, result, "3参数不完全匹配，2参数不优于defConstr → 返回defConstr");
    }

    // ======================== 边界场景 ========================

    @Test
    void testDefConstrIsMoreParamThanAllCandidates_returnsDefConstr() {
        // defConstr 参数数比所有候选都多，循环直接 break
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(ThreeConstrClass.class));

        ConstrEggg twoParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 2) {
                twoParamConstr = c;
                break;
            }
        }
        assertNotNull(twoParamConstr);

        Set<String> keys = keysOf("x", "y", "extra");
        ConstrEggg result = cw.matchConstrEggg(keys, twoParamConstr);
        // 所有 candidate 的 paramCount <= defConstr.getParamCount() → 全部 break
        assertSame(twoParamConstr, result);
    }

    @Test
    void testDefConstrNullWithKeys_throwsNPE() {
        // defConstr 传 null 会触发 NPE（快速路径2 对 null 调用 getParamCount）
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));
        Set<String> keys = keysOf("name", "age");

        assertThrows(NullPointerException.class, () -> {
            cw.matchConstrEggg(keys, null);
        });
    }

    @Test
    void testKeysAreSupersetOfParams_stillFullMatch() {
        // keys 超集：包含所有参数名 + 多余的 key
        ClassEggg cw = eggg.getClassEggg(eggg.getTypeEggg(MultiConstrClass.class));

        ConstrEggg twoParamConstr = null;
        for (ConstrEggg c : cw.getConstrEgggs()) {
            if (c.getParamCount() == 2) {
                twoParamConstr = c;
                break;
            }
        }
        assertNotNull(twoParamConstr);

        Set<String> keys = keysOf("name", "age", "city", "extra1", "extra2");

        ConstrEggg result = cw.matchConstrEggg(keys, twoParamConstr);
        assertEquals(3, result.getParamCount(), "keys 超集应匹配到 3 参数构造器");
    }
}
