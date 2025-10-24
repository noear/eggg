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
        TypeEggg typeEggg = eggg.getTypeEggg(Object.class);

        ClassEggg classEggg = typeEggg.getClassEggg();

        ConstrEggg constrWrap = classEggg.getConstrWrap();

        Assertions.assertNull(constrWrap);

        for (PropertyEggg p1 : classEggg.getPropertyWraps()) {
            System.out.println(p1);
        }
    }

    @Test
    public void case2() {
        TypeEggg typeEggg = eggg.getTypeEggg(UserModel.class);

        ClassEggg classEggg = typeEggg.getClassEggg();

        ConstrEggg constrWrap = classEggg.getConstrWrap();

        Assertions.assertNotNull(constrWrap);
        Assertions.assertEquals(0, constrWrap.getParamCount());

        for (PropertyEggg p1 : classEggg.getPropertyWraps()) {
            System.out.println(p1);
        }

        Assertions.assertEquals(1, classEggg.getPropertyWraps().size());
    }

    @Test
    public void case3() {
        TypeEggg typeEggg = eggg.getTypeEggg(new MyList<UserModel>() {
        }.getClass());

        for (MethodEggg mw : typeEggg.getClassEggg().getPublicMethodWraps()) {
            System.out.println(mw);
        }
    }

    @Test
    public void case4() {
        TypeEggg typeEggg = eggg.getTypeEggg(new HashMap<Integer, UserModel>() {}.getClass());

        if (typeEggg.isMap()) {
            if (typeEggg.isParameterizedType()) {
                //已经分析过的
                Type keyType = typeEggg.getActualTypeArguments()[0];
                Type ValueType = typeEggg.getActualTypeArguments()[1];

                assert keyType.equals(Integer.class);
                assert ValueType.equals(UserModel.class);
                return;
            }
        }

        assert false;
    }
}
