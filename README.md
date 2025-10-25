<h1 align="center" style="text-align:center;">
  EggG
</h1>
<p align="center">
	<strong>A Java Generic Analysis Tool (Generic Egg)</strong>
</p>
<p align="center">
    <a target="_blank" href="https://deepwiki.com/noear/eggg">
        <img src="https://deepwiki.com/badge.svg" alt="Ask DeepWiki" />
    </a>
    <a target="_blank" href="https://central.sonatype.com/artifact/org.noear/eggg">
        <img src="https://img.shields.io/maven-central/v/org.noear/eggg.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.txt">
		<img src="https://img.shields.io/:license-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/downloads/">
		<img src="https://img.shields.io/badge/JDK-25-green.svg" alt="jdk-25" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/eggg/stargazers'>
        <img src='https://gitee.com/noear/eggg/badge/star.svg' alt='gitee star'/>
    </a>
    <a target="_blank" href='https://github.com/noear/eggg/stargazers'>
        <img src="https://img.shields.io/github/stars/noear/eggg.svg?style=flat&logo=github" alt="github star"/>
    </a>
</p>

<hr />

##### Language: English | [中文](README_CN.md)

<hr />


### About EggG

Java Generic analysis is a small tool (about 30k). It covers types, classes, constructors, methods, fields, properties, parameters, extension propagation, and more. Suitable for: framework projects involving generics.

### Example 1

```java
public class EgggDemo {
    //一般，应用内全局单例
    private static Eggg eggg = new Eggg();

    @Test
    public void case1() {
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
```


### Example 2 (Nested Transmission)

```java
public class EgggDemo {
    //一般，应用内全局单例
    private static Eggg eggg = new Eggg();

    @Test
    public void case2() {
        ClassEggg classEggg = eggg.getTypeEggg(C.class).getClassEggg();

        assert classEggg.getFieldEgggByName("x").getType() == List.class;
        assert classEggg.getFieldEgggByName("x").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("x").getTypeEggg().getActualTypeArguments()[0] == String.class;

        assert classEggg.getFieldEgggByName("y").getType() == Map.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().isParameterizedType();
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[0] == String.class;
        assert classEggg.getFieldEgggByName("y").getTypeEggg().getActualTypeArguments()[1] == Integer.class;

        assert classEggg.getFieldEgggByName("m").getType() == String.class;
        assert classEggg.getFieldEgggByName("n").getType() == Integer.class;
    }

    public static class A<X, Y> {
        public X x;
        public Y y;
    }

    public static class B<M, N> extends A<List<M>, Map<String, N>> {
        public M m;
        public N n;
    }

    public static class C extends B<String, Integer> {

    }
}
```


### Example 3 (for snack4)

This example needs to generate refinements, aliases, and specify constructors based on the annotation. Custom content needs to be added.

```java
package org.noear.snack4.codec.util;

import org.noear.eggg.*;
import org.noear.snack4.annotation.ONodeAttrHolder;
import org.noear.snack4.annotation.ONodeAttr;
import org.noear.snack4.annotation.ONodeCreator;

import java.lang.reflect.*;

public class EgggUtil {
    //一般，应用内全局单例
    private static final Eggg eggg = new Eggg()
            .withCreatorClass(ONodeCreator.class)
            .withDigestHandler(EgggUtil::doDigestHandle)
            .withAliasHandler(EgggUtil::doAliasHandle);

    private static String doAliasHandle(ClassEggg cw, Object h, Object digest) {
        if (digest instanceof ONodeAttrHolder) {
            return ((ONodeAttrHolder) digest).getAlias();
        } else {
            return null;
        }
    }

    private static ONodeAttrHolder doDigestHandle(ClassEggg cw, Object h, AnnotatedElement e, ONodeAttrHolder ref) {
        ONodeAttr attr = e.getAnnotation(ONodeAttr.class);

        if (attr == null && ref != null) {
            return ref;
        }

        if (h instanceof FieldEggg) {
            return new ONodeAttrHolder(attr, ((Field) e).getName());
        } else if (h instanceof PropertyMethodEggg) {
            return new ONodeAttrHolder(attr, Property.resolvePropertyName(((Method) e).getName()));
        } else if (h instanceof ParamEggg) {
            return new ONodeAttrHolder(attr, ((Parameter) e).getName());
        } else {
            return null;
        }
    }

    /**
     * 获取类型包装器
     */
    public static TypeEggg getTypeEggg(Type type) {
        return eggg.getTypeEggg(type);
    }
}
```


```java
public class Demo {
    public void case1(){
        ypeEggg typeEggg =  EgggUtil.getTypeEggg(clazz);

        for (FieldEggg fw : typeEggg.getClassEggg().getFieldEgggs()) {
            if (fw.isStatic()) {
                continue;
            }

            //已经分析过的泛型
            fw.getTypeEggg();
        }
    }
}
```