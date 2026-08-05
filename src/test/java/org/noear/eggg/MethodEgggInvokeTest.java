package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 {@link MethodEggg#invoke(Object, Object...)} 调用路径的全面测试。
 *
 * <p>覆盖：返回值类型（引用/基本/装箱/void/null）、静态与实例、私有/受保护的
 * 惰性 setAccessible、参数个数校验、参数类型不符、null 传原始类型、业务异常包装
 * （含业务 CCE/NPE）、异常透传解包、varargs 归一、重载解析、继承/泛型桥接、
 * 首调解析与多次/并发调用的句柄复用等行为契约。
 */
class MethodEgggInvokeTest {

    private final Eggg eggg = new Eggg();

    private MethodEggg methodEgggOf(Class<?> owner, String name, Class<?>... paramTypes) throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(owner));
        Method method = paramTypes.length == 0
                ? tryGetMethod(owner, name)
                : owner.getDeclaredMethod(name, paramTypes);
        return eggg.newMethodEggg(classEggg, method);
    }

    private Method tryGetMethod(Class<?> owner, String name) throws NoSuchMethodException {
        try {
            return owner.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return owner.getMethod(name);
        }
    }

    // ---------------------------------------------------------------------
    // 被测目标类型
    // ---------------------------------------------------------------------

    static class Target {
        public String echo(String s) { return s; }
        public int add(int a, int b) { return a + b; }
        public long addLong(long a, long b) { return a + b; }
        public boolean not(boolean b) { return !b; }
        public double half(double d) { return d / 2; }
        public Integer boxed(Integer i) { return i; }
        public void noReturn() { /* void */ }
        public String returnsNull() { return null; }
        public String noArg() { return "noArg"; }

        public String take(Object o) { return String.valueOf(o); }

        // 业务异常：方法体主动抛
        public void throwsChecked() throws Exception { throw new Exception("boom-checked"); }
        public void throwsRuntime() { throw new IllegalStateException("boom-runtime"); }
        public void throwsBusinessCce() { Object o = "x"; Integer i = (Integer) o; System.out.println(i); }
        public void throwsBusinessNpe() { String s = null; s.length(); }

        public static String staticEcho(String s) { return s; }
        public static int staticAdd(int a, int b) { return a + b; }
        public static void staticVoid() { /* no-op */ }

        // varargs
        public String joinVar(String sep, String... parts) {
            return String.join(sep, parts);
        }
        public int sumVar(int... nums) {
            int s = 0;
            for (int n : nums) s += n;
            return s;
        }

        private String privateEcho(String s) { return "private:" + s; }
        protected String protectedEcho(String s) { return "protected:" + s; }

        // 重载
        public String over() { return "over0"; }
        public String over(String s) { return "over1:" + s; }
        public String over(String s, int n) { return "over2:" + s + n; }
    }

    static class Base<T> {
        public T identity(T t) { return t; }
    }

    static class StringChild extends Base<String> {
    }

    // ---------------------------------------------------------------------
    // 返回值类型
    // ---------------------------------------------------------------------

    @Test
    void invoke_referenceReturn() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "echo", String.class);
        assertEquals("hello", m.invoke(new Target(), "hello"));
    }

    @Test
    void invoke_intReturn_autoboxed() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "add", int.class, int.class);
        Object r = m.invoke(new Target(), 2, 3);
        assertEquals(Integer.valueOf(5), r);
    }

    @Test
    void invoke_longReturn() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "addLong", long.class, long.class);
        assertEquals(7L, (long) (Long) m.invoke(new Target(), 3L, 4L));
    }

    @Test
    void invoke_booleanReturn() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "not", boolean.class);
        assertEquals(Boolean.TRUE, m.invoke(new Target(), false));
    }

    @Test
    void invoke_doubleReturn() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "half", double.class);
        assertEquals(2.5d, (Double) m.invoke(new Target(), 5.0d), 0.0d);
    }

    @Test
    void invoke_boxedParamAndReturn() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "boxed", Integer.class);
        assertEquals(Integer.valueOf(99), m.invoke(new Target(), 99));
    }

    @Test
    void invoke_voidReturnsNull() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "noReturn");
        assertNull(m.invoke(new Target()));
    }

    @Test
    void invoke_methodReturningNull() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "returnsNull");
        assertNull(m.invoke(new Target()));
    }

    @Test
    void invoke_noArgMethod() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "noArg");
        assertEquals("noArg", m.invoke(new Target()));
    }

    @Test
    void invoke_nullArgsTreatedAsEmpty_forNoArgMethod() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "noArg");
        // 显式传 null 的 varargs 数组，应等价于空参
        assertEquals("noArg", m.invoke(new Target(), (Object[]) null));
    }

    @Test
    void invoke_nullPassedToObjectParam() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "take", Object.class);
        assertEquals("null", m.invoke(new Target(), new Object[]{null}));
    }

    // ---------------------------------------------------------------------
    // 静态方法
    // ---------------------------------------------------------------------

    @Test
    void invoke_staticWithNullTarget() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "staticEcho", String.class);
        assertEquals("s", m.invoke(null, "s"));
    }

    @Test
    void invoke_staticIgnoresTarget() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "staticAdd", int.class, int.class);
        // 传一个非 null target，静态调用应忽略它
        assertEquals(Integer.valueOf(10), m.invoke(new Target(), 4, 6));
    }

    @Test
    void invoke_staticVoid() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "staticVoid");
        assertNull(m.invoke(null));
    }

    // ---------------------------------------------------------------------
    // 非 public 方法：惰性 setAccessible
    // ---------------------------------------------------------------------

    @Test
    void invoke_privateMethod() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "privateEcho", String.class);
        assertEquals("private:x", m.invoke(new Target(), "x"));
    }

    @Test
    void invoke_protectedMethod() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "protectedEcho", String.class);
        assertEquals("protected:y", m.invoke(new Target(), "y"));
    }

    // ---------------------------------------------------------------------
    // 参数个数校验：IllegalArgumentException
    // ---------------------------------------------------------------------

    @Test
    void invoke_tooFewArgs_throwsIAE() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "add", int.class, int.class);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> m.invoke(new Target(), 1));
        assertTrue(ex.getMessage().contains("expected 2"));
        assertTrue(ex.getMessage().contains("got 1"));
    }

    @Test
    void invoke_tooManyArgs_throwsIAE() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "echo", String.class);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> m.invoke(new Target(), "a", "b"));
        assertTrue(ex.getMessage().contains("expected 1"));
        assertTrue(ex.getMessage().contains("got 2"));
    }

    @Test
    void invoke_noArgMethodWithArg_throwsIAE() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "noArg");
        assertThrows(IllegalArgumentException.class,
                () -> m.invoke(new Target(), "unexpected"));
    }

    // ---------------------------------------------------------------------
    // 参数类型 / receiver 不符：按 invokeExact 方案归入 ITE
    // ---------------------------------------------------------------------

    @Test
    void invoke_wrongParamType_wrapped() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "echo", String.class);
        // 期望 String，却传 Integer —— 类型转换失败，归入 ITE（invokeExact 方案的固有代价）
        assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target(), 123));
    }

    @Test
    void invoke_nullToPrimitive_wrapped() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "add", int.class, int.class);
        // null 拆箱到 int 会 NPE，归入 ITE
        assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target(), null, 3));
    }

    @Test
    void invoke_wrongReceiverType_wrapped() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "echo", String.class);
        // target 类型不对
        assertThrows(InvocationTargetException.class,
                () -> m.invoke("not-a-Target", "x"));
    }

    // ---------------------------------------------------------------------
    // 业务异常：包装为 InvocationTargetException 且可解包出原始异常
    // ---------------------------------------------------------------------

    @Test
    void invoke_checkedException_wrappedAndUnwrappable() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "throwsChecked");
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target()));
        assertNotNull(ex.getCause());
        assertEquals("boom-checked", ex.getCause().getMessage());
    }

    @Test
    void invoke_runtimeException_wrappedAndUnwrappable() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "throwsRuntime");
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target()));
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertEquals("boom-runtime", ex.getCause().getMessage());
    }

    @Test
    void invoke_businessCce_wrapped() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "throwsBusinessCce");
        // 方法体内部业务 CCE，应包成 ITE（对齐反射语义），而不是被误判为参数错误
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target()));
        assertTrue(ex.getCause() instanceof ClassCastException);
    }

    @Test
    void invoke_businessNpe_wrapped() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "throwsBusinessNpe");
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> m.invoke(new Target()));
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    // ---------------------------------------------------------------------
    // varargs 归一
    // ---------------------------------------------------------------------

    @Test
    void invoke_varargs_arrayPassedAsWhole() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "joinVar", String.class, String[].class);
        // varargs 归一后，数组应作为整体传入而非被当作单元素收集
        assertEquals("a-b-c", m.invoke(new Target(), "-", new String[]{"a", "b", "c"}));
    }

    @Test
    void invoke_varargs_emptyArray() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "joinVar", String.class, String[].class);
        assertEquals("", m.invoke(new Target(), "-", new String[]{}));
    }

    @Test
    void invoke_primitiveVarargs() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "sumVar", int[].class);
        assertEquals(Integer.valueOf(6), m.invoke(new Target(), new int[]{1, 2, 3}));
    }

    // ---------------------------------------------------------------------
    // 重载解析
    // ---------------------------------------------------------------------

    @Test
    void invoke_overloadedResolution() throws Exception {
        assertEquals("over0", methodEgggOf(Target.class, "over").invoke(new Target()));
        assertEquals("over1:a", methodEgggOf(Target.class, "over", String.class).invoke(new Target(), "a"));
        assertEquals("over2:b7", methodEgggOf(Target.class, "over", String.class, int.class).invoke(new Target(), "b", 7));
    }

    // ---------------------------------------------------------------------
    // 继承 / 泛型桥接
    // ---------------------------------------------------------------------

    @Test
    void invoke_inheritedGenericMethod() throws Exception {
        ClassEggg classEggg = eggg.getClassEggg(eggg.getTypeEggg(StringChild.class));
        Method method = Base.class.getMethod("identity", Object.class);
        MethodEggg m = eggg.newMethodEggg(classEggg, method);
        assertEquals("gen", m.invoke(new StringChild(), "gen"));
    }

    // ---------------------------------------------------------------------
    // 句柄复用：首调解析后，多次调用行为稳定
    // ---------------------------------------------------------------------

    @Test
    void invoke_repeatedCalls_consistent() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "echo", String.class);
        Target t = new Target();
        for (int i = 0; i < 1000; i++) {
            assertEquals("v" + i, m.invoke(t, "v" + i));
        }
    }

    @Test
    void invoke_firstCallResolvesLazily_secondCallReuses() throws Exception {
        MethodEggg m = methodEgggOf(Target.class, "add", int.class, int.class);
        // 第一次触发惰性解析
        assertEquals(Integer.valueOf(3), m.invoke(new Target(), 1, 2));
        // 第二次复用已解析句柄
        assertEquals(Integer.valueOf(30), m.invoke(new Target(), 10, 20));
    }

    // ---------------------------------------------------------------------
    // 并发首调：双检锁保证仅解析一次且结果正确
    // ---------------------------------------------------------------------

    @Test
    void invoke_concurrentFirstCall_threadSafe() throws Exception {
        final MethodEggg m = methodEgggOf(Target.class, "add", int.class, int.class);
        final int threads = 16;
        final int perThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger failures = new AtomicInteger();
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    Target t = new Target();
                    for (int j = 0; j < perThread; j++) {
                        Object r = m.invoke(t, j, j);
                        if (!Integer.valueOf(j + j).equals(r)) {
                            failures.incrementAndGet();
                        }
                    }
                } catch (Throwable e) {
                    failures.incrementAndGet();
                }
            }));
        }

        start.countDown();
        for (java.util.concurrent.Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, failures.get(), "concurrent invocations should all succeed with correct results");
    }
}
