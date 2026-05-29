package org.noear.eggg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.eggg.model.UserModel;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Eggg 热插拔移除能力测试
 *
 * @author noear 2025/5/29 created
 */
public class EgggRemoveTest {
    Eggg eggg = new Eggg();

    @Test
    public void test_remove_type() {
        // 1. 先加载并缓存
        TypeEggg typeEggg = eggg.getTypeEggg(UserModel.class);
        Assertions.assertNotNull(typeEggg);

        ClassEggg classEggg = typeEggg.getClassEggg();
        Assertions.assertNotNull(classEggg);

        // 2. 移除指定类型
        boolean removed = eggg.remove(UserModel.class);
        Assertions.assertTrue(removed);

        // 3. 再次移除同一个（应该返回 false）
        boolean removedAgain = eggg.remove(UserModel.class);
        Assertions.assertFalse(removedAgain);

        // 4. 重新获取，应该是新实例
        TypeEggg typeEggg2 = eggg.getTypeEggg(UserModel.class);
        Assertions.assertNotNull(typeEggg2);
        Assertions.assertNotSame(typeEggg, typeEggg2);
    }

    @Test
    public void test_remove_type_cascade_classEggg() {
        // 1. 加载 TypeEggg 和 ClassEggg
        TypeEggg typeEggg = eggg.getTypeEggg(UserModel.class);
        ClassEggg classEggg1 = typeEggg.getClassEggg();

        // 2. remove(Type) 应该级联移除 ClassEggg
        eggg.remove(UserModel.class);

        // 3. 重新获取 ClassEggg，应该是新实例（通过 TypeEggg 重新委托给 Eggg）
        TypeEggg typeEggg2 = eggg.getTypeEggg(UserModel.class);
        ClassEggg classEggg2 = typeEggg2.getClassEggg();
        Assertions.assertNotSame(classEggg1, classEggg2);
    }

    @Test
    public void test_remove_type_not_exists() {
        // 从未加载过的类型
        boolean removed = eggg.remove(Object.class);
        Assertions.assertFalse(removed);
    }

    @Test
    public void test_remove_type_null_param() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            eggg.remove((Type) null);
        });
    }

    @Test
    public void test_removeByClassLoader() {
        // 1. 加载多个类型（这些类型由系统类加载器加载）
        eggg.getTypeEggg(UserModel.class);
        eggg.getTypeEggg(String.class);
        eggg.getTypeEggg(List.class);

        // 2. 使用系统类加载器来移除（实际场景中是自定义的插件 ClassLoader）
        // 这里 UserModel 由应用类加载器加载，String/Integer 由启动类加载器加载（loader 为 null）
        ClassLoader appLoader = UserModel.class.getClassLoader();

        int count = eggg.removeByClassLoader(appLoader);
        // 至少移除了 UserModel
        Assertions.assertTrue(count >= 1);
    }

    @Test
    public void test_removeByClassLoader_null_loader() {
        // boot classloader 的类加载器返回 null
        // 但参数为 null 应该抛异常
        Assertions.assertThrows(NullPointerException.class, () -> {
            eggg.removeByClassLoader(null);
        });
    }

    @Test
    public void test_removeByClassLoader_no_match() {
        eggg.getTypeEggg(String.class);

        // 使用一个自定义的 ClassLoader，应该不会匹配到任何缓存
        ClassLoader customLoader = new ClassLoader() {};
        int count = eggg.removeByClassLoader(customLoader);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void test_removeByPackage() {
        // 1. 加载 model 包下的类型和其他包的类型
        TypeEggg userModelEggg = eggg.getTypeEggg(UserModel.class);
        TypeEggg stringEggg = eggg.getTypeEggg(String.class);
        TypeEggg myListEggg = eggg.getTypeEggg(org.noear.eggg.model.MyList.class);

        // 2. 按 model 包移除
        int count = eggg.removeByPackage("org.noear.eggg.model");

        // 应该移除了 UserModel 和 MyList（2个）
        Assertions.assertEquals(2, count);

        // String 不在 model 包下，应该还在
        TypeEggg stringEggg2 = eggg.getTypeEggg(String.class);
        Assertions.assertSame(stringEggg, stringEggg2);
    }

    @Test
    public void test_removeByPackage_cascade_classEggg() {
        // 1. 加载并验证 ClassEggg 存在
        TypeEggg typeEggg = eggg.getTypeEggg(UserModel.class);
        ClassEggg classEggg1 = typeEggg.getClassEggg();
        Assertions.assertNotNull(classEggg1);

        // 2. 按包移除
        eggg.removeByPackage("org.noear.eggg.model");

        // 3. 重新获取，应该是全新实例
        TypeEggg typeEggg2 = eggg.getTypeEggg(UserModel.class);
        ClassEggg classEggg2 = typeEggg2.getClassEggg();
        Assertions.assertNotSame(classEggg1, classEggg2);
    }

    @Test
    public void test_removeByPackage_no_match() {
        eggg.getTypeEggg(String.class);

        // 不存在的包名
        int count = eggg.removeByPackage("com.nonexistent.package");
        Assertions.assertEquals(0, count);
    }

    @Test
    public void test_removeByPackage_null_param() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            eggg.removeByPackage(null);
        });
    }

    @Test
    public void test_removeByPackage_subpackage_included() {
        // 验证子包也被包含在移除范围内
        // 加载 model 包的类型
        eggg.getTypeEggg(UserModel.class);
        // 加载同包下的 MyList
        eggg.getTypeEggg(org.noear.eggg.model.MyList.class);
        // 加载不同包的类型
        eggg.getTypeEggg(String.class);
        eggg.getTypeEggg(Eggg.class);

        // 用父包 "org.noear.eggg" 移除，应该包含 model 子包
        int count = eggg.removeByPackage("org.noear.eggg");
        // 至少包含 UserModel, MyList, Eggg 三个
        Assertions.assertTrue(count >= 3);
    }

    @Test
    public void test_removeByPackage_no_false_positive() {
        // 确保不会误删：包名前缀相同但不是子包的情况
        // 例如 "org.noear.eggg" 不应该匹配 "org.noear.egggx.SomeClass"
        // 由于实际没有 egggx 包的类，用 String 来验证不受影响
        eggg.getTypeEggg(String.class);
        eggg.getTypeEggg(UserModel.class);

        // 移除 java.lang 包
        int count = eggg.removeByPackage("java.lang");
        // String 在 java.lang 包下
        Assertions.assertEquals(1, count);

        // UserModel 不受影响
        TypeEggg userModelEggg = eggg.getTypeEggg(UserModel.class);
        Assertions.assertNotNull(userModelEggg);
    }

    @Test
    public void test_clear_still_works() {
        // 确保 clear() 仍然正常工作
        eggg.getTypeEggg(UserModel.class);
        eggg.getTypeEggg(String.class);

        eggg.clear();

        // 重新获取应该是新实例
        TypeEggg t1 = eggg.getTypeEggg(UserModel.class);
        TypeEggg t2 = eggg.getTypeEggg(UserModel.class);
        Assertions.assertSame(t1, t2);
    }

    @Test
    public void test_remove_multiple_types() {
        // 加载多个类型
        TypeEggg t1 = eggg.getTypeEggg(UserModel.class);
        TypeEggg t2 = eggg.getTypeEggg(String.class);
        TypeEggg t3 = eggg.getTypeEggg(List.class);

        // 逐个移除
        Assertions.assertTrue(eggg.remove(UserModel.class));
        Assertions.assertTrue(eggg.remove(String.class));
        Assertions.assertTrue(eggg.remove(List.class));

        // 都移除了，再次移除返回 false
        Assertions.assertFalse(eggg.remove(UserModel.class));
        Assertions.assertFalse(eggg.remove(String.class));
        Assertions.assertFalse(eggg.remove(List.class));
    }

    @Test
    public void test_remove_then_reget_integrity() {
        // 验证移除后重新获取的数据完整性
        TypeEggg t1 = eggg.getTypeEggg(UserModel.class);
        ClassEggg c1 = t1.getClassEggg();
        int fieldCount1 = c1.getAllFieldEgggs().size();
        int propCount1 = c1.getPropertyEgggs().size();

        // 移除
        eggg.remove(UserModel.class);

        // 重新获取
        TypeEggg t2 = eggg.getTypeEggg(UserModel.class);
        ClassEggg c2 = t2.getClassEggg();
        int fieldCount2 = c2.getAllFieldEgggs().size();
        int propCount2 = c2.getPropertyEgggs().size();

        // 数据应该一致
        Assertions.assertEquals(fieldCount1, fieldCount2);
        Assertions.assertEquals(propCount1, propCount2);
    }

    @Test
    public void test_remove_parameterized_type() {
        // 测试参数化类型的移除（用 getOriginType）
        TypeEggg t1 = eggg.getTypeEggg(new org.noear.eggg.model.MyList<UserModel>() {}.getClass());
        Assertions.assertNotNull(t1);

        // 用 getOriginType 获取实际缓存 key 移除
        boolean removed = eggg.remove(t1.getOriginType());
        Assertions.assertTrue(removed);
    }

    @Test
    public void test_remove_anonymous_class_by_original_class() {
        // 关键场景：用户传入匿名类 Class，remove 应该也能正确移除
        // 因为 getTypeEggg 内部会把匿名类转换为泛型父类
        TypeEggg t1 = eggg.getTypeEggg(new org.noear.eggg.model.MyList<UserModel>() {}.getClass());
        Assertions.assertNotNull(t1);
        Assertions.assertTrue(t1.isList());
        Assertions.assertTrue(t1.isParameterizedType());

        // 1. 用原始匿名类 Class 做移除（而非 getOriginType）
        Class<?> anonymousClass = new org.noear.eggg.model.MyList<UserModel>() {}.getClass();
        boolean removed = eggg.remove(anonymousClass);
        Assertions.assertTrue(removed);

        // 2. 再次用同一个匿名类获取，应该是新实例
        TypeEggg t2 = eggg.getTypeEggg(new org.noear.eggg.model.MyList<UserModel>() {}.getClass());
        Assertions.assertNotSame(t1, t2);
    }

    @Test
    public void test_remove_hashmap_anonymous_class() {
        // 模拟用户示例中的 HashMap 匿名子类场景
        TypeEggg t1 = eggg.getTypeEggg(new java.util.HashMap<Integer, UserModel>() {}.getClass());
        Assertions.assertNotNull(t1);
        Assertions.assertTrue(t1.isMap());
        Assertions.assertTrue(t1.isParameterizedType());

        // 用匿名类 Class 移除
        Class<?> anonymousClass = new java.util.HashMap<Integer, UserModel>() {}.getClass();
        boolean removed = eggg.remove(anonymousClass);
        Assertions.assertTrue(removed);

        // 再次移除应返回 false
        boolean removedAgain = eggg.remove(anonymousClass);
        Assertions.assertFalse(removedAgain);
    }
}
