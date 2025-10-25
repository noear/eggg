package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 *
 * @author noear 2025/10/24 created
 *
 */
public class GenericTest {
    Eggg eggg = new Eggg();

    @Test
    public void case1() {
        Map<String, Object> map = new HashMap<>();

        eggg.getTypeEggg(map.getClass()).getClassEggg();
    }

    @Test
    public void case2() {
        ClassEggg classEggg = eggg.getTypeEggg(C.class).getClassEggg();

        assert classEggg.getFieldEgggByName("x").getType() == List.class;
        assert classEggg.getFieldEgggByName("x").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("x").getTypeEggg().getActualTypeArguments()[0] == String.class;

        assert classEggg.getFieldEgggByName("y").getType() == Map.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[0] == String.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[1] == Integer.class;

        assert classEggg.getFieldEgggByName("m").getType() == String.class;
        assert classEggg.getFieldEgggByName("n").getType() == Integer.class;
    }

    public static class A<X, Y> {
        public X x;
        public Y y;
    }

    public static class B<M, N> extends A<List<M>, Map<String, N>> {
        public M m;
        public N n;
    }

    public static class C extends B<String, Integer> {

    }
}
