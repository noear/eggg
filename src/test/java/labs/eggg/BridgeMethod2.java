package labs.eggg;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author noear 2025/11/23 created
 *
 */
public class BridgeMethod2 {

    @Test
    public void case1() {
        for(Method m1: PageResult.class.getDeclaredMethods()){
            System.out.println(m1);
        }

        System.out.println("--------");

        for(Method m1: PageResult.class.getMethods()){
            System.out.println(m1);
        }


        /**
         * public void labs.eggg.BridgeMethod2$PageResult.setData(labs.eggg.BridgeMethod2$PageData)
         * public void labs.eggg.BridgeMethod2$PageResult.setData(java.lang.Object)
         * public labs.eggg.BridgeMethod2$PageData labs.eggg.BridgeMethod2$PageResult.getData()
         * public java.lang.Object labs.eggg.BridgeMethod2$PageResult.getData()
         * --------
         * public void labs.eggg.BridgeMethod2$PageResult.setData(labs.eggg.BridgeMethod2$PageData)
         * public void labs.eggg.BridgeMethod2$PageResult.setData(java.lang.Object)
         * public labs.eggg.BridgeMethod2$PageData labs.eggg.BridgeMethod2$PageResult.getData()
         * public java.lang.Object labs.eggg.BridgeMethod2$PageResult.getData()
         * public java.lang.String labs.eggg.BridgeMethod2$R.getMsg()
         * public int labs.eggg.BridgeMethod2$R.getCode()
         * public void labs.eggg.BridgeMethod2$R.setId(java.lang.String)
         * public void labs.eggg.BridgeMethod2$R.setCode(int)
         * public void labs.eggg.BridgeMethod2$R.setMsg(java.lang.String)
         * public java.lang.String labs.eggg.BridgeMethod2$R.getId()
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

    public static class PageResult<T> extends R<PageData<T>> {
        private PageData<T> data;

        @Override
        public PageData<T> getData() {
            return data;
        }

        @Override
        public void setData(PageData<T> data) {
            this.data = data;
        }
    }

    public static class PageData<T> {
        private long page;
        private long size;
        private long total;
        private List<T> list;

        public long getPage() {
            return page;
        }

        public long getSize() {
            return size;
        }

        public long getTotal() {
            return total;
        }

        public List<T> getList() {
            return list;
        }

        public void setPage(long page) {
            this.page = page;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public void setList(List<T> list) {
            this.list = list;
        }
    }

    public static class R<T> {
        /**
         * 本次请求的id，通常用于日志打印排查
         */
        private String id = "bb9f852a-b9a9-4593-891a-0d7f46d02001";
        /**
         * 响应码
         */
        private int code;
        /**
         * 消息
         */
        private String msg = "操作成功";
        /**
         * 响应数据
         */
        private T data;

        public String getId() {
            return id;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public T getData() {
            return data;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
