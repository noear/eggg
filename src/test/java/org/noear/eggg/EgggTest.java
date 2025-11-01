package org.noear.eggg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.eggg.model.MyList;
import org.noear.eggg.model.UserModel;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
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
        TypeEggg typeEggg = eggg.getTypeEggg(Object.class);

        ClassEggg classEggg = typeEggg.getClassEggg();

        ConstrEggg constrEggg = classEggg.getCreator();

        Assertions.assertNotNull(constrEggg);

        for (FieldEggg p1 : classEggg.getAllFieldEgggs()) {
            System.out.println(p1);
        }

        for (PropertyEggg p1 : classEggg.getPropertyEgggs()) {
            System.out.println(p1);
        }
    }

    @Test
    public void case2() {
        TypeEggg typeEggg = eggg.getTypeEggg(UserModel.class);

        ClassEggg classEggg = typeEggg.getClassEggg();

        ConstrEggg constrEggg = classEggg.getCreator();

        Assertions.assertNotNull(constrEggg);
        Assertions.assertEquals(0, constrEggg.getParamCount());

        for (PropertyEggg p1 : classEggg.getPropertyEgggs()) {
            System.out.println(p1);
        }

        Assertions.assertEquals(1, classEggg.getPropertyEgggs().size());
    }

    @Test
    public void case3() {
        TypeEggg typeEggg = eggg.getTypeEggg(new MyList<UserModel>() {
        }.getClass());

        for (MethodEggg mw : typeEggg.getClassEggg().getPublicMethodEgggs()) {
            System.out.println(mw);
        }
    }

    @Test
    public void case4() {
        TypeEggg typeEggg = eggg.getTypeEggg(new HashMap<Integer, UserModel>() {
        }.getClass());

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

    @Test
    public void case5() {
        TypeEggg typeEggg = eggg.getTypeEggg(List.class);
        assert typeEggg.isList();
        assert typeEggg.getGenericInfo().size() == 1;
        assert typeEggg.getGenericInfo().get("E") instanceof TypeVariable;
    }

    @Test
    public void case6() {
        TypeEggg typeEggg = eggg.getTypeEggg(Map.class);
        assert typeEggg.isMap();
        assert typeEggg.getGenericInfo().size() == 2;
        assert typeEggg.getGenericInfo().get("K") instanceof TypeVariable;
        assert typeEggg.getGenericInfo().get("V") instanceof TypeVariable;
    }
}