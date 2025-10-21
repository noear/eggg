package org.noear.eggg;

/**
 *
 * @author noear 2025/10/21 created
 *
 */
public class EgggDemo {
    Eggg eggg = new Eggg();

    public void case1() {
        TypeWrap typeWrap = eggg.getTypeWrap(EgggDemo.class);

        ClassWrap classWrap = typeWrap.getClassWrap();

        classWrap.getFieldNameMap();
    }
}
