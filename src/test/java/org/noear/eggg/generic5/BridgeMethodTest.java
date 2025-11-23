package org.noear.eggg.generic5;

import org.junit.jupiter.api.Test;
import org.noear.eggg.ClassEggg;
import org.noear.eggg.Eggg;
import org.noear.eggg.MethodEggg;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author noear 2025/11/23 created
 *
 */
public class BridgeMethodTest {
    @Test
    public void case1() {
        Eggg eggg = new Eggg();

        ClassEggg classWrap = eggg.getClassEggg(SysResourcePermissionController.class);

        assert classWrap.getDeclaredMethodEgggs().size() == 0;

        MethodEggg[] methodEgggs = classWrap.getPublicMethodEgggs().toArray(new MethodEggg[0]);

        assert methodEgggs.length == 2;
        assert methodEgggs[0].getName().equals("saveAll");
        assert methodEgggs[0].getGenericReturnType() instanceof ParameterizedType;
        assert methodEgggs[1].getName().equals("saveOne");
        assert methodEgggs[1].getGenericReturnType().equals(SysResourcePermission.class);

        System.out.println(Arrays.toString(SysResourcePermissionController.class.getDeclaredMethods()));
        //=> [public java.util.List com.example.demo.App$SysResourcePermissionController.saveAll(java.util.List)]
    }

    public static class SysResourcePermissionController extends BaseController<SysResourcePermissionService, SysResourcePermission, SysResourcePermissionId> {

    }

    abstract static class BaseController<S extends BaseService<T, ID>, T, ID> {
        protected S service;

        public T saveOne(T ts) {
            return service.saveOne(ts);
        }

        public List<T> saveAll(List<T> ts) {
            return service.saveAll(ts);
        }
    }

    public static class SysResourcePermissionService extends BaseService<SysResourcePermission, SysResourcePermissionId> {
    }

    abstract static class BaseService<T, ID> {
        public T saveOne(T ts) {
            return ts;
        }

        public List<T> saveAll(List<T> ts) {
            return ts;
        }
    }

    public static class SysResourcePermissionId {
    }

    public static class SysResourcePermission {
        public int id;
        public String name;
    }
}
