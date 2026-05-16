/*
 * Copyright 2025 ~ noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.eggg;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 类包装器
 *
 * @author noear
 * @since 1.0
 */
public class ClassEggg implements AnnotatedEggg {
    private final TypeEggg typeEggg;

    private final Object digest;
    private ConstrEggg creator;

    private final List<ConstrEggg> constrEgggs;

    private final Map<String, FieldEggg> allFieldEgggsForName;
    private final Map<String, FieldEggg> allFieldEgggsForAlias;

    private final Map<Method, MethodEggg> ownMethodEgggsMap; //own.public + own.declared
    private final List<MethodEggg> ownMethodEgggs;
    private final List<MethodEggg> publicMethodEgggs;
    private final List<MethodEggg> declaredMethodEgggs;

    private final Map<String, PropertyEggg> propertyEgggsForName = new LinkedHashMap<>();
    private final Map<String, PropertyEggg> propertyEgggsForAlias;

    private boolean likeRecordClass = true;
    private final boolean realRecordClass;

    private final Eggg eggg;

    public ClassEggg(Eggg eggg, TypeEggg typeEggg) {
        Objects.requireNonNull(eggg, "eggg");
        Objects.requireNonNull(typeEggg, "typeEggg");

        this.eggg = eggg;
        this.typeEggg = typeEggg;
        this.realRecordClass = JavaUtil.isRecordClass(typeEggg.getType()); //不能放下面(构造器要用到)

        //1.加载字段
        this.allFieldEgggsForName = new LinkedHashMap<>();
        loadFields();

        this.likeRecordClass = likeRecordClass && allFieldEgggsForName.size() > 0;
        this.allFieldEgggsForAlias = new LinkedHashMap<>(allFieldEgggsForName.size());
        for (Map.Entry<String, FieldEggg> entry : allFieldEgggsForName.entrySet()) {
            allFieldEgggsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }


        //2.加载方法
        Method[] declaredMethods = eggg.getDeclaredMethods(typeEggg.getType());
        Method[] methods = eggg.getMethods(typeEggg.getType());

        ownMethodEgggs = new ArrayList<>(declaredMethods.length + methods.length);
        ownMethodEgggsMap = new HashMap<>(declaredMethods.length + methods.length);

        if (methods.length == 0) {
            publicMethodEgggs = Collections.emptyList();
        } else {
            publicMethodEgggs = new ArrayList<>(methods.length);
        }
        if (declaredMethods.length == 0) {
            declaredMethodEgggs = Collections.emptyList();
        } else {
            declaredMethodEgggs = new ArrayList<>(declaredMethods.length);
        }

        loadMethods(declaredMethods, methods);

        propertyEgggsForAlias = new LinkedHashMap<>(propertyEgggsForName.size());
        for (Map.Entry<String, PropertyEggg> entry : propertyEgggsForName.entrySet()) {
            propertyEgggsForAlias.put(entry.getValue().getAlias(), entry.getValue());
        }

        //3.加构造器（顺序不能乱）
        Constructor[] declaredConstructors = typeEggg.getType().getDeclaredConstructors();
        constrEgggs = new ArrayList<>(declaredConstructors.length);
        loadConstr(declaredConstructors);


        this.digest = eggg.findDigest(this, this, null);
    }

    /**
     * 真实的记录类
     */
    public boolean isRealRecordClass() {
        return realRecordClass;
    }

    /**
     * 疑似的记录类
     */
    public boolean isLikeRecordClass() {
        return likeRecordClass;
    }

    public TypeEggg getTypeEggg() {
        return typeEggg;
    }

    public Class<?> getType() {
        return typeEggg.getType();
    }

    public Type getGenericType() {
        return typeEggg.getGenericType();
    }

    @Override
    public AnnotatedElement getElement() {
        return typeEggg.getType();
    }

    /**
     * 获取提炼物
     */
    public <T extends Object> T getDigest() {
        return (T) digest;
    }

    private Annotation[] annotations;

    @Override
    public Annotation[] getAnnotations() {
        if (annotations == null) {
            annotations = typeEggg.getType().getAnnotations();
        }
        return annotations;
    }

    /**
     * 获取创造器
     */
    public ConstrEggg getCreator() {
        return creator;
    }

    /**
     * 获取所有构造器（不可变视图）
     */
    public List<ConstrEggg> getConstrEgggs() {
        return Collections.unmodifiableList(constrEgggs);
    }

    public ConstrEggg findConstrEggg(Class<?>... parameterTypes) throws NoSuchMethodException {
        ConstrEggg c1 = findConstrEgggOrNull(parameterTypes);

        if (c1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + ".()" + argumentTypesToString(parameterTypes));
        } else {
            return c1;
        }
    }

    public ConstrEggg findConstrEgggOrNull(Class<?>... parameterTypes) throws NoSuchMethodException {
        for (ConstrEggg c1 : constrEgggs) {
            if (c1.getParamCount() == parameterTypes.length) {
                if (parameterTypes.length == 0) {
                    return c1;
                } else {
                    if (Arrays.equals(c1.getConstr().getParameterTypes(), parameterTypes)) {
                        return c1;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 根据可用 key 集合，从所有构造器中选择最佳匹配的创造器。
     * 优先选择：参数全部匹配（matchCount == paramCount）且参数最多的构造器。
     * 如果没有完全匹配的，返回 defCreator。
     *
     * <p>性能优化：
     * <ul>
     *   <li>快速路径：构造器数<=1、无参默认构造器、有参构造器仅1个时直接返回</li>
     *   <li>constrEgggs 已在 loadConstr 中按参数数降序排序，首个全匹配即为最优，可立即返回</li>
     * </ul>
     *
     * @param availableKeys 可用 key 集合
     * @param defCreator    默认创造器（通常来自 getCreator()）
     * @return 最佳匹配的构造器
     */
    public ConstrEggg matchBestCreator(Set<String> availableKeys, ConstrEggg defCreator) {
        if (availableKeys == null || availableKeys.isEmpty()) {
            return defCreator;
        }

        // 快速路径1：只有一个构造器，无需选择
        if (constrEgggs.size() <= 1) {
            return defCreator;
        }

        // 快速路径2：默认构造器是无参的，无需匹配
        if (defCreator.getParamCount() == 0) {
            return defCreator;
        }

        // 快速路径3：只有一个有参构造器（就是 defCreator 本身），无需匹配
        int paramConstructorCount = 0;
        for (ConstrEggg c : constrEgggs) {
            if (c.getParamCount() > 0) {
                paramConstructorCount++;
            }
        }
        if (paramConstructorCount <= 1) {
            return defCreator;
        }

        // 完整匹配：constrEgggs 已按参数数降序排序，首个全匹配即为最优
        for (ConstrEggg candidate : constrEgggs) {
            int paramCount = candidate.getParamCount();
            // 已降序排列，后续不可能更优
            if (paramCount <= defCreator.getParamCount()) {
                break;
            }

            String[] aliases = candidate.getParamAliasAry();
            int matchCount = 0;
            for (String alias : aliases) {
                if (availableKeys.contains(alias)) {
                    matchCount++;
                }
            }

            // 严格全匹配：所有参数都有对应 key，直接返回（降序排列保证是最多参数的）
            if (matchCount == paramCount) {
                return candidate;
            }
        }

        return defCreator;
    }

    public Collection<MethodEggg> getPublicMethodEgggs() {
        return publicMethodEgggs;
    }

    public Collection<MethodEggg> getDeclaredMethodEgggs() {
        return declaredMethodEgggs;
    }

    public MethodEggg findMethodEggg(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        MethodEggg m1 = findMethodEgggOrNull(name, parameterTypes);

        if (m1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + "." + name + argumentTypesToString(parameterTypes));
        } else {
            return m1;
        }
    }

    public MethodEggg findMethodEgggOrNull(String name, Class<?>... parameterTypes) {
        for (MethodEggg m1 : declaredMethodEgggs) {
            if (m1.getParamCount() == parameterTypes.length && m1.getName().equals(name)) {
                if (parameterTypes.length == 0) {
                    return m1;
                } else {
                    if (Arrays.equals(m1.getMethod().getParameterTypes(), parameterTypes)) {
                        return m1;
                    }
                }
            }
        }

        for (MethodEggg m1 : publicMethodEgggs) {
            if (m1.getParamCount() == parameterTypes.length && m1.getName().equals(name)) {
                if (parameterTypes.length == 0) {
                    return m1;
                } else {
                    if (Arrays.equals(m1.getMethod().getParameterTypes(), parameterTypes)) {
                        return m1;
                    }
                }
            }
        }

        return null;
    }

    public MethodEggg findMethodEgggOrNew(Method method) {
        return ownMethodEgggsMap.computeIfAbsent(method, k -> eggg.newMethodEggg(this, k));
    }

    public Collection<MethodEggg> getOwnMethodEgggs() {
        return ownMethodEgggs;
    }

    public Collection<FieldEggg> getAllFieldEgggs() {
        return allFieldEgggsForName.values();
    }

    public FieldEggg getFieldEgggByName(String name) {
        return allFieldEgggsForName.get(name);
    }

    public FieldEggg getFieldEgggByAlias(String alias) {
        return allFieldEgggsForAlias.get(alias);
    }

    public Collection<PropertyEggg> getPropertyEgggs() {
        return propertyEgggsForName.values();
    }

    public PropertyEggg getPropertyEgggByName(String name) {
        return propertyEgggsForName.get(name);
    }

    public PropertyEggg getPropertyEgggByAlias(String alias) {
        return propertyEgggsForAlias.get(alias);
    }

    /// /////////////////

    protected void loadConstr(Constructor[] declaredConstructors) {
        //加载构造器
        for (Constructor c1 : declaredConstructors) {
            constrEgggs.add(new ConstrEggg(eggg, this, c1, eggg.findCreator(c1)));
        }

        //先从静态方法找
        if (typeEggg.getType().isEnum()) {
            for (MethodEggg me : declaredMethodEgggs) {
                if (me.isStatic()) {
                    boolean isCreator = eggg.findCreator(me.getMethod());
                    if (isCreator) {
                        creator = eggg.newConstrEggg(this, me.getMethod(), isCreator);
                        return;
                    }
                }
            }
        }

        //再从构造方法找
        for (ConstrEggg c1 : constrEgggs) {
            if (c1.isCreator()) {
                creator = c1;
                return;
            } else if (creator == null) {
                //初始化
                creator = c1;
            } else if (creator.getParamCount() > c1.getParamCount()) {
                //谁参数少，用谁
                creator = c1;
            }
        }

        //按参数数量降序排序，供 matchBestCreator 使用（首个全匹配即为最优，可提前退出）
        constrEgggs.sort((a, b) -> Integer.compare(b.getParamCount(), a.getParamCount()));
    }

    protected void loadFields() {
        Class<?> clz = typeEggg.getType();

        while (clz != null) {
            for (Field f1 : eggg.getDeclaredFields(clz)) {
                allFieldEgggsForName.computeIfAbsent(f1.getName(), kn->{
                    //不能用 put 接收（会有重名的私有字段）
                    //
                    FieldEggg fe = eggg.newFieldEggg(this, f1);

                    if (fe.isStatic() == false) {
                        //如果全是只读，则
                        likeRecordClass = likeRecordClass && fe.isFinal();
                        propertyEgggsForName.computeIfAbsent(fe.getName(), k -> new PropertyEggg(k))
                                .setFieldEggg(fe);
                    }

                    return fe;
                });
            }

            clz = clz.getSuperclass();
        }
    }

    protected void loadMethods(Method[] declaredMethods, Method[] methods) {
        for (Method m1 : declaredMethods) {
            if (m1.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m1.isBridge() == false) {
                MethodEggg me = eggg.newMethodEggg(this, m1);

                declaredMethodEgggs.add(me);

                if (me.isPublic() == false) {
                    //如果是公有，由公有处添加
                    ownMethodEgggs.add(me);
                    ownMethodEgggsMap.put(m1, me);
                }
            }
        }

        for (Method m1 : methods) {
            if (m1.getDeclaringClass() == Object.class) {
                continue;
            }

            if (m1.isBridge()) {
                // 桥接方法：在继承链中解析为实际方法
                m1 = findActualMethod(m1);
                if (m1 == null) {
                    continue;
                }
            } else if (m1.getDeclaringClass() != typeEggg.getType()) {
                // 非桥接但从父类继承的方法：尝试在声明类中找到更具体的版本（保留泛型签名）
                Method moreSpecific = findDeclaredMethodInChain(m1);
                if (moreSpecific != null) {
                    m1 = moreSpecific;
                }
            }

            // 去重：按方法名+参数类型查找是否已添加（处理覆写方法和桥接方法解析后的重复）
            MethodEggg existing = findExistingMethodEggg(m1);
            if (existing != null) {
                continue;
            }

            MethodEggg me = eggg.newMethodEggg(this, m1);
            publicMethodEgggs.add(me);
            ownMethodEgggs.add(me);
            ownMethodEgggsMap.put(m1, me);

            if (me.isStatic() == false && me.isPublic()) {
                //非静态、公有的才可能是属性方法
                String m1N = m1.getName();
                if (m1N.length() > 2) {
                    if (m1.getReturnType() == void.class && m1.getParameterCount() == 1) {
                        //setter
                        if (m1N.length() > 3 && m1N.startsWith("set")) {
                            PropertyMethodEggg sw = eggg.newPropertyMethodEggg(this, me);

                            propertyEgggsForName.computeIfAbsent(sw.getName(), k -> new PropertyEggg(k))
                                    .setSetterEggg(sw);
                        }
                    } else if (m1.getReturnType() != void.class && m1.getParameterCount() == 0) {
                        //getter
                        if ((m1N.length() > 3 && m1N.startsWith("get")) ||
                                (m1N.length() > 2 && m1N.startsWith("is"))) {
                            PropertyMethodEggg gw = eggg.newPropertyMethodEggg(this, me);

                            propertyEgggsForName.computeIfAbsent(gw.getName(), k -> new PropertyEggg(k))
                                    .setGetterEggg(gw);
                        }
                    }
                }
            }
        }
    }

    /**
     * 在 ownMethodEgggsMap 中按方法名和参数类型查找已存在的 MethodEggg（用于 bridge 方法去重）
     */
    private MethodEggg findExistingMethodEggg(Method method) {
        for (Map.Entry<Method, MethodEggg> entry : ownMethodEgggsMap.entrySet()) {
            Method key = entry.getKey();
            if (key.getName().equals(method.getName())
                    && Arrays.equals(key.getParameterTypes(), method.getParameterTypes())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 桥接方法解析：在继承链中查找同名非桥接方法
     */
    private Method findActualMethod(Method m1) {
        Class<?> clz = typeEggg.getType();
        while (clz != null && clz != Object.class) {
            for (Method dm : clz.getDeclaredMethods()) {
                if (dm.getName().equals(m1.getName()) && !dm.isBridge()) {
                    return dm;
                }
            }
            clz = clz.getSuperclass();
        }
        return null;
    }

    /**
     * 对从父类继承的非桥接方法，尝试在声明类中找到带有更完整泛型信息的 declared 版本
     */
    private Method findDeclaredMethodInChain(Method m1) {
        Class<?> clz = m1.getDeclaringClass();
        for (Method dm : clz.getDeclaredMethods()) {
            if (dm.getName().equals(m1.getName())
                    && !dm.isBridge()
                    && Arrays.equals(dm.getParameterTypes(), m1.getParameterTypes())) {
                return dm;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return typeEggg.hashCode();
    }

    @Override
    public String toString() {
        return typeEggg.toString();
    }

    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }
}
