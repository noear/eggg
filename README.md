<h1 align="center" style="text-align:center;">
  EggG
</h1>
<p align="center">
	<strong>A Java type metadata analysis and building, and fluent reflective invocation tool (generics, annotations, distill, aliasing, caching)</strong>
</p>
<p align="center">
    <a href="https://deepwiki.com/noear/eggg"><img src="https://deepwiki.com/badge.svg" alt="Ask DeepWiki"></a>
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

A tool for analyzing and building Java type metadata, and fluent reflective invocation. It covers details of types, classes, constructors, methods, fields, properties, parameters, generic propagation, and more. Suitable for: framework projects involving generics and annotations.


### Dependency

```xml
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>eggg</artifactId>
    <version>1.1.4</version>
</dependency>
```



### Example 0 (Fluent Reflective Invocation)

```java
public class EgggDemo {
    // 一般应用内全局单例
    private static Eggg eggg = new Eggg();

    @Test
    public void case0() {
        Eggg eggg = new Eggg();

        // Start from class: create instance -> invoke method
        String result = eggg.reflect(String.class) // result = "World"
                .create("Hello World")
                .call("substring", 6)  // method invoke
                .get();

        // Start from instance: direct invocation
        String result2 = eggg.reflect("Hello World") // result2 = "World"
                .call("substring", 6)  // method invoke
                .get();
        
        // Field read/write + chaining
        Person person = eggg.reflect(Person.class)
                .create()
                .setField("name", "Tom")     // field write
                .setField("age", 25)         // field write
                .get();
        
        String name = eggg.reflect(person).field("name").get(); // field read -> "Tom"

        // Property read/write (via getter/setter)
        Person p = eggg.reflect(Person.class).create()
                .setProperty("name", "Alice")  // via setName
                .setProperty("age", 30)        // via setAge
                .get();
        String name = eggg.reflect(p).property("name").get(); // via getName -> "Alice"

        // Invoke static method
        String s = eggg.reflect(Person.class)
                .call("staticHello")
                .get();

        // Primitive and wrapper types auto-interop
        Person p2 = eggg.reflect(Person.class)
                .create("Bob", 30)  // Integer auto-matches int parameter
                .get();
    }
}
```


### Example 1 (Type metadata analysis)

```java
public class EgggDemo {
    // Generally, application-wide singleton
    private static Eggg eggg = new Eggg();

    @Test
    public void case1() {
        Class<?> type = new HashMap<Integer, UserModel>() {}.getClass();
        TypeEggg typeEggg = eggg.getTypeEggg(type);

        if (typeEggg.isMap()) {
            if (typeEggg.isParameterizedType()) {
                // The analyzed generic information
                Type keyType = typeEggg.getActualTypeArguments()[0];
                Type ValueType = typeEggg.getActualTypeArguments()[1];

                assert keyType.equals(Integer.class);
                assert ValueType.equals(UserModel.class);
            } else {
                assert false;
            }
        } else {
            assert false;
        }

        //If it is a hot-swappable project, the cache can be removed after use.
        eggg.remove(type);
    }
}
```


### Example 2 (Generalized Nested Argument Analysis)

```java
public class EgggDemo {
    // Generally, application-wide singleton
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


### Example 3 (for Snack4)

This example demonstrates how to generate refinements, aliases, and specify constructors based on annotations. Custom content needs to be added.

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
        TypeEggg typeEggg = EgggUtil.getTypeEggg(clazz);

        for (FieldEggg fw : typeEggg.getClassEggg().getFieldEgggs()) {
            if (fw.isStatic()) {
                continue;
            }

            // Analyzed generic info
            fw.getTypeEggg();
        }
    }
}
```


### Example 4 (for Solon)

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