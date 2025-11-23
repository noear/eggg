package org.noear.eggg.issue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.eggg.ClassEggg;
import org.noear.eggg.Eggg;
import org.noear.eggg.PropertyEggg;

import java.util.List;

/**
 *
 * @author noear 2025/11/23 created
 *
 */
public class Issue_ID828T_BridgeMethod {

    @Test
    public void case1() {
        Eggg eggg = new Eggg();

        PageResult<String> pageResult1 = new PageResult<>();

        ClassEggg classEggg = eggg.getClassEggg(pageResult1.getClass());
        PropertyEggg pe = classEggg.getPropertyEgggByName("data");

        Assertions.assertEquals(PageResult.class, pe.getGetterEggg().getMethod().getDeclaringClass());
        Assertions.assertEquals(4, classEggg.getPropertyEgggs().size());
        Assertions.assertEquals(2, classEggg.getDeclaredMethodEgggs().size());
        Assertions.assertEquals(8, classEggg.getPublicMethodEgggs().size());
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