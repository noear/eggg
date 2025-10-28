package org.noear.eggg.generic;

import org.junit.jupiter.api.Test;
import org.noear.eggg.Eggg;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author noear 2024/10/30 created
 */
public class GenericUtilTest {
    private Eggg eggg = new Eggg();

    @Test
    public void case2() {
        List<Type> tmp = eggg.findGenericList(eggg.getTypeEggg(DemoService.class), ServiceImplEx.class);

        System.out.println(tmp);

        assert tmp.size() == 2;
        assert tmp.get(0) == DemoMapper.class;
        assert tmp.get(1) == Demo.class;
    }

    @Test
    public void case3() {
        List<Type> tmp = eggg.findGenericList(eggg.getTypeEggg(DemoService.class), ServiceImpl.class);

        System.out.println(tmp);

        assert tmp.size() == 2;
        assert tmp.get(0) == DemoMapper.class;
        assert tmp.get(1) == Demo.class;
    }

    @Test
    public void case4() {
        List<Type> tmp = eggg.findGenericList(eggg.getTypeEggg(DemoImpl.class), Map.class);
        System.out.println(tmp);

        assert tmp.size() == 2;
        assert tmp.get(0) == Integer.class;
        assert tmp.get(1) == String.class;

        tmp = eggg.findGenericList(eggg.getTypeEggg(DemoImpl.class), IDemo.class);
        System.out.println(tmp);

        assert tmp.size() == 1;
        assert tmp.get(0) == Double.class;
    }

    @Test
    public void case5() {
        List<Type> tmp = eggg.findGenericList(eggg.getTypeEggg(DemoHashImpl.class), Map.class);
        System.out.println(tmp);

        assert tmp.size() == 2;
        assert tmp.get(0) == Integer.class;
        assert tmp.get(1) == String.class;

        tmp = eggg.findGenericList(eggg.getTypeEggg(DemoHashImpl.class), IDemo.class);
        System.out.println(tmp);

        assert tmp.size() == 1;
        assert tmp.get(0) == Double.class;
    }

    @Test
    public void case6() {
        List<Type> tmp = eggg.findGenericList(eggg.getTypeEggg(UserMapperI.class), MapperI.class);
        System.out.println(tmp);

        assert tmp.size() == 1;
        assert tmp.get(0) == UserD.class;
    }

    private interface IDemo<T> {
    }

    private abstract static class DemoImpl implements Map<Integer, String>, IDemo<Double> {

    }

    private abstract static class DemoHashImpl extends HashMap<Integer, String> implements IDemo<Double> {

    }

    private class UserD {

    }

    public interface MapperI<T> {
    }

    public interface UserMapperI extends MapperI<UserD> {
    }
}