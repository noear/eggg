package org.noear.eggg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 *
 * @author noear
 * @since 1.0
 */
public class ReflectHandlerDefault implements ReflectHandler {
    private static ReflectHandler instance = new ReflectHandlerDefault();

    public static ReflectHandler getInstance() {
        return instance;
    }

    public static void setInstance(ReflectHandler instance) {
        Objects.requireNonNull(instance, "instance");

        ReflectHandlerDefault.instance = instance;
    }

    @Override
    public Field[] getDeclaredFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    @Override
    public Method[] getDeclaredMethods(Class<?> clazz) {
        return clazz.getDeclaredMethods();
    }

    @Override
    public Method[] getMethods(Class<?> clazz) {
        return clazz.getMethods();
    }
}