package demo.eggg;

import org.noear.eggg.ClassEggg;
import org.noear.eggg.Eggg;
import org.noear.eggg.TypeEggg;

/**
 *
 * @author noear 2025/10/21 created
 *
 */
public class EgggDemo {
    Eggg eggg = new Eggg();

    public void case1() {
        TypeEggg typeEggg = eggg.getTypeEggg(EgggDemo.class);

        ClassEggg classEggg = typeEggg.getClassEggg();

        classEggg.getAllFieldEgggs();
    }
}
