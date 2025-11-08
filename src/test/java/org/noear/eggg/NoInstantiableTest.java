package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.util.function.Function;

/**
 *
 * @author noear 2025/11/7 created
 *
 */
public class NoInstantiableTest {
    Eggg eggg = new Eggg();

    @Test
    public void case1() {
        ClassEggg classEggg = eggg.getClassEggg(Function.class);
        System.out.println(classEggg);
        assert classEggg.getCreator() == null;
    }

    @Test
    public void case2() {
        ClassEggg classEggg = eggg.getClassEggg(ElementType.class);
        System.out.println(classEggg);
        System.out.println(classEggg.getCreator());
        assert classEggg.getCreator() != null;
    }
}
