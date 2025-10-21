package org.noear.eggg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 *
 * @author noear 2025/10/21 created
 *
 */
public class EgggTest {
    Eggg eggg = new Eggg();

    @Test
    public void case1() {
        TypeWrap typeWrap = eggg.getTypeWrap(Object.class);

        ClassWrap<Object> classWrap = typeWrap.getClassWrap();

        ConstrWrap constrWrap = classWrap.getConstrWrap();

        Assertions.assertNull(constrWrap);

        for (Map.Entry<String, PropertyWrap<Object>> entry : classWrap.getPropertyAliasWraps().entrySet()) {

        }
    }
}
