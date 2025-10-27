package org.noear.eggg.demo;

import org.junit.jupiter.api.Test;
import org.noear.eggg.ClassEggg;
import org.noear.eggg.Eggg;
import org.noear.eggg.TypeEggg;
import org.noear.eggg.model.UserModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        TypeEggg typeEggg = eggg.getTypeEggg(new HashMap<Integer, UserModel>() {}.getClass());

        if (typeEggg.isMap()) {
            if (typeEggg.isParameterizedType()) {
                //已经分析过的
                Type keyType = typeEggg.getActualTypeArguments()[0];
                Type ValueType = typeEggg.getActualTypeArguments()[1];

                assert keyType.equals(Integer.class);
                assert ValueType.equals(UserModel.class);
                return;
            }
        }

        assert false;
    }

    @Test
    public void case1_2() {
        TypeEggg typeEggg = eggg.getTypeEggg(new HashMap<String,HashMap<Integer, UserModel>>() {}.getClass());

        if (typeEggg.isMap()) {
            if (typeEggg.isParameterizedType()) {
                //已经分析过的
                Type keyType = typeEggg.getActualTypeArguments()[0];
                Type ValueType = typeEggg.getActualTypeArguments()[1];

                assert keyType.equals(String.class);
                assert ValueType instanceof ParameterizedType;

                ParameterizedType valueType2 = (ParameterizedType) ValueType;
                valueType2.getActualTypeArguments()[0].equals(Integer.class);
                valueType2.getActualTypeArguments()[1].equals(UserModel.class);
                return;
            }
        }

        assert false;
    }

    @Test
    public void case2() {
        ClassEggg classEggg = eggg.getTypeEggg(C.class).getClassEggg();

        assert classEggg.getFieldEgggByName("x").getTypeEggg().getType() == List.class;
        assert classEggg.getFieldEgggByName("x").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("x").getTypeEggg().getActualTypeArguments()[0] == String.class;

        assert classEggg.getFieldEgggByName("y").getTypeEggg().getType() == Map.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[0] == String.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[1] == Integer.class;

        assert classEggg.getFieldEgggByName("m").getTypeEggg().getType() == String.class;
        assert classEggg.getFieldEgggByName("n").getTypeEggg().getType() == Integer.class;
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
