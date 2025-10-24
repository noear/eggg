package org.noear.eggg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.eggg.model.MyList;
import org.noear.eggg.model.UserModel;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 *
 * @author noear 2025/10/21 created
 *
 */
public class EgggTest {
    Eggg eggg = new Eggg();

    @Test
    public void case1() {
        TypeEggg typeWrap = eggg.getTypeWrap(Object.class);

        ClassEggg classWrap = typeWrap.getClassWrap();

        ConstrEggg constrWrap = classWrap.getConstrWrap();

        Assertions.assertNull(constrWrap);

        for (PropertyEggg p1 : classWrap.getPropertyWraps()) {
            System.out.println(p1);
        }
    }

    @Test
    public void case2() {
        TypeEggg typeWrap = eggg.getTypeWrap(UserModel.class);

        ClassEggg classWrap = typeWrap.getClassWrap();

        ConstrEggg constrWrap = classWrap.getConstrWrap();

        Assertions.assertNotNull(constrWrap);
        Assertions.assertEquals(0, constrWrap.getParamCount());

        for (PropertyEggg p1 : classWrap.getPropertyWraps()) {
            System.out.println(p1);
        }

        Assertions.assertEquals(1, classWrap.getPropertyWraps().size());
    }

    @Test
    public void case3() {
        TypeEggg typeWrap = eggg.getTypeWrap(new MyList<UserModel>() {
        }.getClass());

        for (MethodEggg mw : typeWrap.getClassWrap().getPublicMethodWraps()) {
            System.out.println(mw);
        }
    }

    @Test
    public void case4() {
        TypeEggg typeWrap = eggg.getTypeWrap(new HashMap<Integer, UserModel>() {}.getClass());

        if (typeWrap.isMap()) {
            if (typeWrap.isParameterizedType()) {
                //已经分析过的
                Type keyType = typeWrap.getActualTypeArguments()[0];
                Type ValueType = typeWrap.getActualTypeArguments()[1];

                assert keyType.equals(Integer.class);
                assert ValueType.equals(UserModel.class);
                return;
            }
        }

        assert false;
    }
}
