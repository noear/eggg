package demo.eggg;

import org.noear.eggg.ClassWrap;
import org.noear.eggg.Eggg;
import org.noear.eggg.TypeWrap;

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

        classWrap.getFieldWraps();
    }
}
