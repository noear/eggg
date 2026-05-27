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
 * 类包装器，提供对类的反射元数据访问，包括字段、方法、构造器和属性的统一封装
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

    private final Map<String, List<MethodEggg>> methodIndexForName;

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


        // 4.构建方法查找索引（按 name 分组）
        methodIndexForName = new HashMap<>(ownMethodEgggs.size());
        for (MethodEggg me : ownMethodEgggs) {
            methodIndexForName.computeIfAbsent(me.getName(), k -> new ArrayList<>(2)).add(me);
        }

        this.digest = eggg.findDigest(this, this, null);
    }

    /**
     * 是否为 Java Record 类（java.lang.Record 的子类）
     */
    public boolean isRealRecordClass() {
        return realRecordClass;
    }

    /**
     * 是否为疑似记录类（所有字段均为 final 的普通类，行为类似 Record）
     */
    public boolean isLikeRecordClass() {
        return likeRecordClass;
    }

    /**
     * 获取关联的类型包装器
     */
    public TypeEggg getTypeEggg() {
        return typeEggg;
    }

    /**
     * 获取原始 Class 对象
     */
    public Class<?> getType() {
        return typeEggg.getType();
    }

    /**
     * 获取泛型类型信息（如 ParameterizedType、Class 等）
     */
    public Type getGenericType() {
        return typeEggg.getGenericType();
    }

    @Override
    public AnnotatedElement getElement() {
        return typeEggg.getType();
    }

    /**
     * 获取提炼物（由 Eggg 的 digestHandler 产生的自定义元数据）
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
     * 获取创造器（标记了 @Creator 或参数最少的构造器）
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

    /**
     * 按参数类型查找构造器
     *
     * @param parameterTypes 构造器参数类型
     * @return 匹配的构造器包装器
     * @throws NoSuchMethodException 未找到匹配的构造器
     */
    public ConstrEggg findConstrEggg(Class<?>... parameterTypes) throws NoSuchMethodException {
        ConstrEggg c1 = findConstrEgggOrNull(parameterTypes);

        if (c1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + ".()" + argumentTypesToString(parameterTypes));
        } else {
            return c1;
        }
    }

    /**
     * 按参数类型查找构造器，未找到时返回 null
     *
     * @param parameterTypes 构造器参数类型
     * @return 匹配的构造器包装器，或 null
     */
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
     * @param defConstr    默认创造器（通常来自 getCreator()）
     * @return 最佳匹配的构造器
     */
    public ConstrEggg matchConstrEggg(Set<String> availableKeys, ConstrEggg defConstr) {
        if (availableKeys == null || availableKeys.isEmpty()) {
            return defConstr;
        }

        // 快速路径1：只有一个构造器，无需选择
        if (constrEgggs.size() <= 1) {
            return defConstr;
        }

        // 快速路径2：默认构造器是无参的，无需匹配
        if (defConstr.getParamCount() == 0) {
            return defConstr;
        }

        // 快速路径3：只有一个有参构造器（就是 defCreator 本身），无需匹配
        int paramConstructorCount = 0;
        for (ConstrEggg c : constrEgggs) {
            if (c.getParamCount() > 0) {
                paramConstructorCount++;
            }
        }
        if (paramConstructorCount <= 1) {
            return defConstr;
        }

        // 完整匹配：constrEgggs 已按参数数降序排序，首个全匹配即为最优
        for (ConstrEggg candidate : constrEgggs) {
            int paramCount = candidate.getParamCount();
            // 已降序排列，后续不可能更优
            if (paramCount <= defConstr.getParamCount()) {
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

        return defConstr;
    }

    /**
     * 获取所有公有方法（含继承的，排除桥接方法和 Object 方法）
     */
    public Collection<MethodEggg> getPublicMethodEgggs() {
        return publicMethodEgggs;
    }

    /**
     * 获取所有声明方法（仅当前类声明的，含私有，排除桥接方法和 Object 方法）
     */
    public Collection<MethodEggg> getDeclaredMethodEgggs() {
        return declaredMethodEgggs;
    }

    /**
     * 按名称和参数类型查找方法，先查声明方法再查公有方法
     *
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @return 匹配的方法包装器
     * @throws NoSuchMethodException 未找到匹配的方法
     */
    public MethodEggg findMethodEggg(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        MethodEggg m1 = findMethodEgggOrNull(name, parameterTypes);

        if (m1 == null) {
            throw new NoSuchMethodException(typeEggg.getType().getName() + "." + name + argumentTypesToString(parameterTypes));
        } else {
            return m1;
        }
    }

    /**
     * 按名称和参数类型查找方法，未找到时返回 null
     *
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @return 匹配的方法包装器，或 null
     */
    public MethodEggg findMethodEgggOrNull(String name, Class<?>... parameterTypes) {
        List<MethodEggg> candidates = methodIndexForName.get(name);
        if (candidates == null) return null;

        for (MethodEggg m1 : candidates) {
            if (m1.getParamCount() == parameterTypes.length) {
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

    /**
     * 按反射 Method 查找方法包装器，不存在则创建并缓存
     */
    public MethodEggg findMethodEgggOrNew(Method method) {
        return ownMethodEgggsMap.computeIfAbsent(method, k -> eggg.newMethodEggg(this, k));
    }

    /**
     * 获取自有方法（公有方法 + 声明的私有方法，去重合并）
     */
    public Collection<MethodEggg> getOwnMethodEgggs() {
        return ownMethodEgggs;
    }

    /**
     * 获取所有字段（含父类的，子类同名字段优先）
     */
    public Collection<FieldEggg> getAllFieldEgggs() {
        return allFieldEgggsForName.values();
    }

    /**
     * 按字段名查找字段
     *
     * @param name 字段名
     * @return 字段包装器，或 null
     */
    public FieldEggg getFieldEgggByName(String name) {
        return allFieldEgggsForName.get(name);
    }

    /**
     * 按别名查找字段
     *
     * @param alias 字段别名
     * @return 字段包装器，或 null
     */
    public FieldEggg getFieldEgggByAlias(String alias) {
        return allFieldEgggsForAlias.get(alias);
    }

    /**
     * 获取所有属性（由字段和 getter/setter 方法组合而成）
     */
    public Collection<PropertyEggg> getPropertyEgggs() {
        return propertyEgggsForName.values();
    }

    /**
     * 按属性名查找属性
     *
     * @param name 属性名
     * @return 属性包装器，或 null
     */
    public PropertyEggg getPropertyEgggByName(String name) {
        return propertyEgggsForName.get(name);
    }

    /**
     * 按属性别名查找属性
     *
     * @param alias 属性别名
     * @return 属性包装器，或 null
     */
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

        //按参数数量降序排序，供 findConstrEggg(keys,defConstr) 使用（首个全匹配即为最优，可提前退出）
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
