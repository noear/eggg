package labs.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author noear 2025/11/23 created
 *
 */
public class BridgeMethod1 {
    @Test
    public void case1() {
        for(Method m1: SysResourcePermissionController.class.getDeclaredMethods()){
            System.out.println(m1);
        }

        System.out.println("--------");

        for(Method m1: SysResourcePermissionController.class.getMethods()){
            System.out.println(m1);
        }

        //为什么 SysResourcePermissionController 生成了 saveAll 桥接方法；但是没有生成 saveOne 的？

        /**
         * public java.util.List labs.eggg.BridgeMethod1$SysResourcePermissionController.saveAll(java.util.List)
         * --------
         * public java.util.List labs.eggg.BridgeMethod1$SysResourcePermissionController.saveAll(java.util.List)
         * public java.lang.Object labs.eggg.BridgeMethod1$BaseController.saveOne(java.lang.Object)
         * public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
         * public final native void java.lang.Object.wait(long) throws java.lang.InterruptedException
         * public final void java.lang.Object.wait() throws java.lang.InterruptedException
         * public boolean java.lang.Object.equals(java.lang.Object)
         * public java.lang.String java.lang.Object.toString()
         * public native int java.lang.Object.hashCode()
         * public final native java.lang.Class java.lang.Object.getClass()
         * public final native void java.lang.Object.notify()
         * public final native void java.lang.Object.notifyAll()
         * */
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
