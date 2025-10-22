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
        TypeWrap typeWrap = eggg.getTypeWrap(new HashMap<Integer, UserModel>() {}.getClass());

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
```


### Example 2 (for snack4)

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

    private static String doAliasHandle(ClassWrap cw, Object h, Object digest) {
        if (digest instanceof ONodeAttrHolder) {
            return ((ONodeAttrHolder) digest).getAlias();
        } else {
            return null;
        }
    }

    private static ONodeAttrHolder doDigestHandle(ClassWrap cw, Object h, AnnotatedElement e, ONodeAttrHolder ref) {
        ONodeAttr attr = e.getAnnotation(ONodeAttr.class);

        if (attr == null && ref != null) {
            return ref;
        }

        if (h instanceof FieldWrap) {
            return new ONodeAttrHolder(attr, ((Field) e).getName());
        } else if (h instanceof PropertyMethodWrap) {
            return new ONodeAttrHolder(attr, Property.resolvePropertyName(((Method) e).getName()));
        } else if (h instanceof ParamWrap) {
            return new ONodeAttrHolder(attr, ((Parameter) e).getName());
        } else {
            return null;
        }
    }

    /**
     * 获取类型包装器
     */
    public static TypeWrap getTypeWrap(Type type) {
        return eggg.getTypeWrap(type);
    }
}
```


```java
public class Demo {
    public void case1(){
        ypeWrap typeWrap =  EgggUtil.getTypeWrap(clazz);

        for (FieldWrap fw : typeWrap.getClassWrap().getFieldWraps()) {
            if (fw.isStatic()) {
                continue;
            }

            //已经分析过的泛型
            fw.getTypeWrap();
        }
    }
}
```