<h1 align="center" style="text-align:center;">
  EggG
</h1>
<p align="center">
	<strong>一个 Java 类型元数据分析与构建工具（泛型、注解、提炼、别名、缓存）</strong>
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

##### 语言： 中文 | [English](README.md)

<hr />


### 关于 EggG

一个 Java 类型元数据分析与构建工具。分析会涉及：类型、类、构造器、方法、字段、属性、参数，泛型传导等细节。适合一些：涉及泛型和注解的框架性项目采用。

### 示例1

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


### 示例2（嵌套传递）

```java
public class EgggDemo {
    //一般，应用内全局单例
    private static Eggg eggg = new Eggg();

    @Test
    public void case2() {
        ClassEggg classEggg = eggg.getTypeEggg(C.class).getClassEggg();

        for(FieldEggg fe : classEggg.getAllFieldEgggs()) {
            fe.<Fastjson2Anno>getDigest();
        }

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

### 示例3 (for snack4)

这个示例需要根据 "注解" 生成提炼物、别名，以及指定构造器。需要添加定制内容。

```java
package org.noear.snack4.codec.util;

import org.noear.eggg.*;
import org.noear.snack4.annotation.ONodeAttrHolder;
import org.noear.snack4.annotation.ONodeAttr;
import org.noear.snack4.annotation.ONodeCreator;

import java.lang.reflect.*;

public class EgggUtil {
    private static final Eggg eggg = new Eggg()
            .withCreatorClass(ONodeCreator.class)
            .withDigestHandler(EgggUtil::doDigestHandle)
            .withAliasHandler(EgggUtil::doAliasHandle);

    private static String doAliasHandle(ClassEggg cw, AnnotatedEggg s, String ref) {
        if (s.getDigest() instanceof ONodeAttrHolder) {
            return ((ONodeAttrHolder) s.getDigest()).getAlias();
        } else {
            return ref;
        }
    }

    private static Object doDigestHandle(ClassEggg cw, AnnotatedEggg s, Object ref) {
        ONodeAttr attr = s.getElement().getAnnotation(ONodeAttr.class);

        if (attr == null && ref != null) {
            return ref;
        }

        if (s instanceof FieldEggg) {
            return new ONodeAttrHolder(attr, ((Field) s.getElement()).getName());
        } else if (s instanceof PropertyMethodEggg) {
            return new ONodeAttrHolder(attr, Property.resolvePropertyName(((Method) s.getElement()).getName()));
        } else if (s instanceof ParamEggg) {
            return new ONodeAttrHolder(attr, ((Parameter) s.getElement()).getName());
        } else {
            return null;
        }
    }

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


### 示例4（for Solon） 

```java
package org.noear.solon.core.util;

import org.noear.eggg.*;
import org.noear.solon.core.wrap.FieldSpec;
import org.noear.solon.core.wrap.ParamSpec;
import org.noear.solon.core.wrap.VarSpec;

import java.lang.reflect.*;

public class EgggUtil {
    private static final Eggg eggg = new Eggg()
            .withAliasHandler(EgggUtil::doAliasHandle)
            .withDigestHandler(EgggUtil::doDigestHandle)
            .withReflectHandler(new EgggReflectHandler());

    private static String doAliasHandle(ClassEggg cw, AnnotatedEggg s, String ref) {
        if (s.getDigest() instanceof VarSpec) {
            return s.<VarSpec>getDigest().getName();
        }

        return ref;
    }

    private static Object doDigestHandle(ClassEggg cw, AnnotatedEggg s, Object ref) {
        if (s instanceof FieldEggg) {
            return new FieldSpec((FieldEggg) s);
        } else if (s instanceof ParamEggg) {
            return new ParamSpec((ParamEggg) s);
        }

        return ref;
    }

    public static TypeEggg getTypeEggg(Type type) {
        return eggg.getTypeEggg(type);
    }

    public static ClassEggg getClassEggg(Type type) {
        return getTypeEggg(type).getClassEggg();
    }
}
```