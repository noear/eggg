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

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EgggReflect 完善单元测试
 *
 * <p>覆盖维度：
 * A. 入口方法（reflectOf）
 * B. create（构造实例）
 * C. call（方法调用）
 * D. field/setField（字段读写）
 * E. property/setProperty（属性读写）
 * F. type() 和 get()
 * G. equals/hashCode/toString
 * H. 核心链式场景
 * I. Eggg 实例隔离与缓存
 * J. 异常场景
 *
 * @author noear
 * @since 1.1
 */
class EgggReflectTest {

    private final Eggg eggg = new Eggg();

    // ==================== 测试模型 ====================

    public static class Person {
        private String name;
        private int age;

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String hello() {
            return "Hello, I'm " + name;
        }

        public String greet(String greeting) {
            return greeting + ", " + name + "!";
        }

        public static String staticHello() {
            return "static hello";
        }

        public void voidMethod() {
            // do nothing
        }
    }

    public static class Animal {
        private String species;

        public Animal() {
        }

        public Animal(String species) {
            this.species = species;
        }

        public String getSpecies() {
            return species;
        }

        public void setSpecies(String species) {
            this.species = species;
        }

        public String makeSound() {
            return "...";
        }
    }

    public static class Dog extends Animal {
        private String name;

        public Dog() {
        }

        public Dog(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String makeSound() {
            return "Woof!";
        }

        // 方法重载
        public String bark() {
            return "woof";
        }

        public String bark(int times) {
            return "woof x" + times;
        }

        public String bark(String prefix) {
            return prefix + " woof";
        }
    }

    public static class Calculator {
        public boolean check(boolean flag) {
            return flag;
        }

        public byte inc(byte b) {
            return (byte) (b + 1);
        }

        public short doubleShort(short s) {
            return (short) (s * 2);
        }

        public int add(int a, int b) {
            return a + b;
        }

        public long multiply(long a, long b) {
            return a * b;
        }

        public float half(float f) {
            return f / 2;
        }

        public double square(double d) {
            return d * d;
        }

        public char nextChar(char c) {
            return (char) (c + 1);
        }
    }

    public static class PrivateAccess {
        private String secret = "hidden";

        private String getSecret() {
            return secret;
        }

        private void setSecret(String s) {
            this.secret = s;
        }
    }

    public static class Book {
        private final String title;
        private final String author;

        public Book(String title, String author) {
            this.title = title;
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getInfo() {
            return title + " by " + author;
        }
    }

    public static class CollectionHolder {
        public String[] getStringArray() {
            return new String[]{"a", "b", "c"};
        }

        public List<String> getStringList() {
            return Arrays.asList("x", "y", "z");
        }

        public Map<String, Integer> getMap() {
            return Collections.singletonMap("key", 1);
        }
    }

    public static class PropertyOnly {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        // 只读属性
        public String getReadOnly() {
            return "readonly";
        }
    }

    public static class VoidService {
        public int counter = 0;

        public void increment() {
            counter++;
        }

        public void add(int n) {
            counter += n;
        }

        public void add(Integer n) {
            counter += n;
        }
    }

    // ==================== A. 入口方法 ====================

    @Test
    void testOnClassByClass() {
        EgggReflect reflect = eggg.reflect(Person.class);
        assertEquals(Person.class, reflect.type());
        assertNull(reflect.get());
    }

    @Test
    void testOnClassByClassName() {
        String result = eggg.reflect("java.lang.String")
                .create("Hello").get();
        assertEquals("Hello", result);
    }

    @Test
    void testOnClassNull() {
        assertThrows(NullPointerException.class, () -> eggg.reflect((Class<?>) null));
    }

    @Test
    void testOnBeanWithObject() {
        Person person = new Person("Tom");
        EgggReflect reflect = eggg.reflect(person);
        assertEquals(Person.class, reflect.type());
        assertSame(person, reflect.get());
    }

    @Test
    void testOnBeanNull() {
        assertThrows(NullPointerException.class, () -> eggg.reflect((Object) null));
    }

    @Test
    void testOnBeanTypePreserved() {
        assertEquals(String.class, eggg.reflect((Object) "hello").type());
    }

    @Test
    void testOnBeanWithInteger() {
        assertEquals(Integer.class, eggg.reflect(42).type());
    }

    // ==================== B. create ====================

    @Test
    void testCreateNoArgs() {
        Person person = eggg.reflect(Person.class).create().get();
        assertNotNull(person);
        assertInstanceOf(Person.class, person);
    }

    @Test
    void testCreateWithSingleArg() {
        Person person = eggg.reflect(Person.class).create("Alice").get();
        assertEquals("Alice", person.getName());
        assertEquals(0, person.getAge());
    }

    @Test
    void testCreateWithMultipleArgs() {
        Person person = eggg.reflect(Person.class).create("Tom", 25).get();
        assertEquals("Tom", person.getName());
        assertEquals(25, person.getAge());
    }

    @Test
    void testCreateString() {
        assertEquals("Hello World", eggg.reflect(String.class).create("Hello World").get());
    }

    @Test
    void testCreateStringNoArgs() {
        assertEquals("", eggg.reflect(String.class).create().get());
    }

    @Test
    void testCreateWithPrimitiveArgs() {
        // Integer 传给 int 参数（模糊匹配）
        Person person = eggg.reflect(Person.class).create("Bob", Integer.valueOf(30)).get();
        assertEquals("Bob", person.getName());
        assertEquals(30, person.getAge());
    }

    @Test
    void testCreateNoMatchingConstructor() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(String.class).create(1, 2, 3, 4, 5));
        assertTrue(ex.getCause() instanceof NoSuchMethodException);
    }

    @Test
    void testCreateReturnsNewReflect() {
        EgggReflect reflect = eggg.reflect(Person.class).create("Test");
        assertNotNull(reflect.get());
        assertEquals(Person.class, reflect.type());
    }

    @Test
    void testCreateChainedWithCall() {
        String result = eggg.reflect(Person.class)
                .create("Tom")
                .call("hello")
                .get();
        assertEquals("Hello, I'm Tom", result);
    }

    @Test
    void testCreateOnlyParameterizedConstructor() {
        Book book = eggg.reflect(Book.class)
                .create("Java Guide", "James")
                .get();
        assertEquals("Java Guide", book.getTitle());
        assertEquals("James", book.getAuthor());
    }

    @Test
    void testCreateWithWrapperArgs() {
        // 传 Integer.valueOf(30) 匹配 Person(String, int) 的 int 参数
        Person person = eggg.reflect(Person.class)
                .create("Bob", Integer.valueOf(30))
                .get();
        assertEquals("Bob", person.getName());
        assertEquals(30, person.getAge());
    }

    @Test
    void testCreateMultipleTimes() {
        EgggReflect classReflect = eggg.reflect(Person.class);
        Person p1 = classReflect.create("A").get();
        Person p2 = classReflect.create("B").get();
        assertNotSame(p1, p2);
        assertEquals("A", p1.getName());
        assertEquals("B", p2.getName());
    }

    // ==================== C. call ====================

    @Test
    void testCallNoArgs() {
        assertEquals("Hello, I'm Tom",
                eggg.reflect(new Person("Tom", 25)).call("hello").get());
    }

    @Test
    void testCallWithArgs() {
        assertEquals("Hi, Tom!",
                eggg.reflect(new Person("Tom", 25)).call("greet", "Hi").get());
    }

    @Test
    void testCallWithPrimitiveArg() {
        Person person = new Person();
        eggg.reflect(person).call("setAge", 25);
        assertEquals(25, person.getAge());
    }

    @Test
    void testCallVoidMethod() {
        Person person = new Person("Tom");
        EgggReflect reflect = eggg.reflect(person).call("voidMethod");
        assertNotNull(reflect);
        assertSame(person, reflect.get());
    }

    @Test
    void testCallVoidMethodReturnsSameObject() {
        VoidService service = new VoidService();
        EgggReflect reflect = eggg.reflect(service).call("increment");
        assertSame(service, reflect.get());
        assertEquals(1, service.counter);
    }

    @Test
    void testCallChained() {
        assertEquals("World",
                eggg.reflect(String.class).create("Hello World")
                        .call("substring", 6).call("toString").get());
    }

    @Test
    void testCallReturnsNull() {
        EgggReflect reflect = eggg.reflect(new Person()).call("getName");
        assertNull(reflect.get());
        // type 应为 String（方法的返回类型）
        assertEquals(String.class, reflect.type());
    }

    @Test
    void testCallStaticMethod() {
        assertEquals("static hello",
                eggg.reflect(Person.class).call("staticHello").get());
    }

    @Test
    void testCallMethodNotFound() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("nonExistentMethod"));
        assertTrue(ex.getCause() instanceof NoSuchMethodException);
    }

    @Test
    void testCallWithWrongArgCount() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("greet", "Hi", "Extra"));
        assertTrue(ex.getCause() instanceof NoSuchMethodException);
    }

    @Test
    void testCallWithWrongArgTypes() {
        // int 不匹配 String 参数
        assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("greet", 123));
    }

    @Test
    void testCallOverloadedNoArgs() {
        assertEquals("woof", eggg.reflect(new Dog()).call("bark").get());
    }

    @Test
    void testCallOverloadedWithInt() {
        assertEquals("woof x3", eggg.reflect(new Dog()).call("bark", 3).get());
    }

    @Test
    void testCallOverloadedWithString() {
        assertEquals("hey woof", eggg.reflect(new Dog()).call("bark", "hey").get());
    }

    @Test
    void testCallInheritedMethod() {
        Dog dog = new Dog();
        dog.setSpecies("Canine");
        assertEquals("Canine", eggg.reflect(dog).call("getSpecies").get());
    }

    @Test
    void testCallOverriddenMethod() {
        assertEquals("Woof!", eggg.reflect(new Dog()).call("makeSound").get());
    }

    @Test
    void testCallPrivateMethod() {
        PrivateAccess pa = new PrivateAccess();
        assertEquals("hidden", eggg.reflect(pa).call("getSecret").get());
    }

    @Test
    void testCallPrivateSetter() {
        PrivateAccess pa = new PrivateAccess();
        eggg.reflect(pa).call("setSecret", "revealed");
        assertEquals("revealed", eggg.reflect(pa).call("getSecret").get());
    }

    @Test
    void testCallAllPrimitiveTypes_boolean() {
        Calculator calc = new Calculator();
        assertTrue(eggg.reflect(calc).call("check", true).<Boolean>get());
    }

    @Test
    void testCallAllPrimitiveTypes_byte() {
        Calculator calc = new Calculator();
        assertEquals((byte) 2, (byte) eggg.reflect(calc).call("inc", (byte) 1).<Byte>get());
    }

    @Test
    void testCallAllPrimitiveTypes_short() {
        Calculator calc = new Calculator();
        assertEquals((short) 20, (short) eggg.reflect(calc).call("doubleShort", (short) 10).<Short>get());
    }

    @Test
    void testCallAllPrimitiveTypes_int() {
        Calculator calc = new Calculator();
        assertEquals(5, (int) eggg.reflect(calc).call("add", 2, 3).get());
    }

    @Test
    void testCallAllPrimitiveTypes_long() {
        Calculator calc = new Calculator();
        assertEquals(6L, (long) eggg.reflect(calc).call("multiply", 2L, 3L).get());
    }

    @Test
    void testCallAllPrimitiveTypes_float() {
        Calculator calc = new Calculator();
        assertEquals(2.5f, eggg.reflect(calc).call("half", 5.0f).<Float>get(), 0.001f);
    }

    @Test
    void testCallAllPrimitiveTypes_double() {
        Calculator calc = new Calculator();
        assertEquals(9.0, eggg.reflect(calc).call("square", 3.0).get());
    }

    @Test
    void testCallAllPrimitiveTypes_char() {
        Calculator calc = new Calculator();
        assertEquals('B', (char) eggg.reflect(calc).call("nextChar", 'A').<Character>get());
    }

    @Test
    void testCallReturnsArray() {
        CollectionHolder holder = new CollectionHolder();
        String[] arr = eggg.reflect(holder).call("getStringArray").get();
        assertArrayEquals(new String[]{"a", "b", "c"}, arr);
    }

    @Test
    void testCallReturnsList() {
        CollectionHolder holder = new CollectionHolder();
        @SuppressWarnings("unchecked")
        List<String> list = eggg.reflect(holder).call("getStringList").get();
        assertEquals(Arrays.asList("x", "y", "z"), list);
    }

    @Test
    void testCallReturnsMap() {
        CollectionHolder holder = new CollectionHolder();
        @SuppressWarnings("unchecked")
        Map<String, Integer> map = eggg.reflect(holder).call("getMap").get();
        assertEquals(1, map.size());
        assertEquals(1, map.get("key"));
    }

    @Test
    void testCallVoidMethodChain() {
        VoidService service = new VoidService();
        eggg.reflect(service).call("increment").call("increment");
        assertEquals(2, service.counter);
    }

    @Test
    void testCallVoidMethodWithArgs() {
        VoidService service = new VoidService();
        eggg.reflect(service).call("add", 5);
        assertEquals(5, service.counter);
    }

    @Test
    void testCallOverloadedIntVsInteger() {
        VoidService service = new VoidService();
        // int 参数精确匹配
        eggg.reflect(service).call("add", 10);
        assertEquals(10, service.counter);
    }

    // ==================== D. field / setField ====================

    @Test
    void testFieldGet() {
        assertEquals("Tom", eggg.reflect(new Person("Tom", 25)).field("name").get());
    }

    @Test
    void testFieldSet() {
        Person person = new Person();
        eggg.reflect(person).setField("name", "Alice").setField("age", 30);
        assertEquals("Alice", person.getName());
        assertEquals(30, person.getAge());
    }

    @Test
    void testFieldSetAndGetChain() {
        assertEquals("Bob",
                eggg.reflect(new Person()).setField("name", "Bob").field("name").get());
    }

    @Test
    void testFieldGetReturnsEgggReflect() {
        EgggReflect reflect = eggg.reflect(new Person("Tom")).field("name");
        assertEquals("Tom", reflect.get());
        assertEquals(String.class, reflect.type());
    }

    @Test
    void testFieldSetReturnsThis() {
        Person person = new Person();
        EgggReflect reflect = eggg.reflect(person).setField("name", "Tom");
        assertSame(person, reflect.get());
    }

    @Test
    void testFieldNotFound() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).field("nonExistentField"));
        assertTrue(ex.getCause() instanceof NoSuchFieldException);
    }

    @Test
    void testFieldSetNotFound() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).setField("nonExistentField", "value"));
        assertTrue(ex.getCause() instanceof NoSuchFieldException);
    }

    @Test
    void testFieldGetNull() {
        Person person = new Person();
        assertNull(eggg.reflect(person).field("name").get());
    }

    @Test
    void testFieldSetNull() {
        Person person = new Person("Tom");
        eggg.reflect(person).setField("name", null);
        assertNull(person.getName());
    }

    @Test
    void testFieldSetInt() {
        Person person = new Person();
        eggg.reflect(person).setField("age", 30);
        assertEquals(30, person.getAge());
    }

    @Test
    void testFieldGetInherited() {
        Dog dog = new Dog();
        dog.setSpecies("Canine");
        assertEquals("Canine", eggg.reflect(dog).field("species").get());
    }

    @Test
    void testFieldSetInherited() {
        Dog dog = new Dog();
        eggg.reflect(dog).setField("species", "Canine");
        assertEquals("Canine", dog.getSpecies());
    }

    @Test
    void testFieldMultipleSets() {
        Person person = new Person();
        eggg.reflect(person)
                .setField("name", "A")
                .setField("age", 1)
                .setField("name", "B")
                .setField("age", 2);
        assertEquals("B", person.getName());
        assertEquals(2, person.getAge());
    }

    // -- 字段一致性 --

    @Test
    void testFieldGetConsistency() {
        Person person = new Person("Tom");
        String v1 = eggg.reflect(person).field("name").get();
        String v2 = eggg.reflect(person).field("name").get();
        assertEquals(v1, v2);
    }

    @Test
    void testSetAliasForSetField() {
        Person person = new Person();
        eggg.reflect(person).setField("name", "Alice");
        assertEquals("Alice", person.getName());
    }

    // ==================== E. property / setProperty ====================

    @Test
    void testPropertyGet() {
        assertEquals("Tom", eggg.reflect(new Person("Tom")).property("name").get());
    }

    @Test
    void testPropertySet() {
        Person person = new Person();
        eggg.reflect(person).setProperty("name", "Alice").setProperty("age", 25);
        assertEquals("Alice", person.getName());
        assertEquals(25, person.getAge());
    }

    @Test
    void testPropertyGetReturnsEgggReflect() {
        EgggReflect reflect = eggg.reflect(new Person("Tom")).property("name");
        assertEquals("Tom", reflect.get());
    }

    @Test
    void testPropertySetReturnsThis() {
        Person person = new Person();
        EgggReflect reflect = eggg.reflect(person).setProperty("name", "Tom");
        assertSame(person, reflect.get());
    }

    @Test
    void testPropertySetAndGetChain() {
        Person person = new Person();
        String name = eggg.reflect(person)
                .setProperty("name", "Alice")
                .property("name").get();
        assertEquals("Alice", name);
    }

    @Test
    void testPropertyReadOnly() {
        PropertyOnly po = new PropertyOnly();
        assertEquals("readonly", eggg.reflect(po).property("readOnly").get());
    }

    @Test
    void testPropertyFallbackToField() {
        // Person 有 name 字段 + getName/setName，所以 property 走 getter
        Person person = new Person("Tom");
        assertEquals("Tom", eggg.reflect(person).property("name").get());
    }

    @Test
    void testPropertyWithPrimitiveType() {
        Person person = new Person();
        eggg.reflect(person).setProperty("age", 30);
        int age = eggg.reflect(person).property("age").get();
        assertEquals(30, age);
    }

    @Test
    void testPropertySetWithNull() {
        Person person = new Person("Tom");
        eggg.reflect(person).setProperty("name", null);
        assertNull(person.getName());
    }

    @Test
    void testPropertyInherited() {
        Dog dog = new Dog();
        dog.setSpecies("Canine");
        assertEquals("Canine", eggg.reflect(dog).property("species").get());
    }

    // ==================== F. type() 和 get() ====================

    @Test
    void testTypeOnClass() {
        assertEquals(Person.class, eggg.reflect(Person.class).type());
    }

    @Test
    void testTypeOnBean() {
        assertEquals(String.class, eggg.reflect((Object) "Hello").type());
    }

    @Test
    void testTypeAfterCreate() {
        assertEquals(Person.class, eggg.reflect(Person.class).create().type());
    }

    @Test
    void testTypeAfterCall() {
        assertEquals(String.class,
                eggg.reflect(new Person("Tom")).call("hello").type());
    }

    @Test
    void testTypeAfterCallNull() {
        assertEquals(String.class,
                eggg.reflect(new Person()).call("getName").type());
    }

    @Test
    void testTypeAfterField() {
        assertEquals(String.class,
                eggg.reflect(new Person("Tom")).field("name").type());
    }

    @Test
    void testGetReturnsCorrectType() {
        String result = eggg.reflect((Object) "Hello").get();
        assertEquals("Hello", result);
    }

    // ==================== G. equals / hashCode / toString ====================

    @Test
    void testEqualsSameObject() {
        Person person = new Person("Tom");
        EgggReflect r1 = eggg.reflect(person);
        EgggReflect r2 = eggg.reflect(person);
        assertEquals(r1, r2);
    }

    @Test
    void testEqualsDifferentObject() {
        assertNotEquals(eggg.reflect(new Person("Tom")), eggg.reflect(new Person("Tom")));
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, eggg.reflect(new Person()));
    }

    @Test
    void testEqualsNonReflect() {
        assertNotEquals("not an EgggReflect", eggg.reflect(new Person()));
    }

    @Test
    void testHashCodeConsistent() {
        Person person = new Person("Tom");
        EgggReflect r1 = eggg.reflect(person);
        assertEquals(person.hashCode(), r1.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        EgggReflect r1 = eggg.reflect((Object) "A");
        EgggReflect r2 = eggg.reflect((Object) "B");
        assertNotEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToStringWithValue() {
        assertEquals("Hello", eggg.reflect((Object) "Hello").toString());
    }

    @Test
    void testToStringWithNull() {
        // onClass 后 object 为 null，toString 返回 "null"
        assertEquals("null", eggg.reflect(Person.class).toString());
    }

    // ==================== H. 核心链式场景 ====================

    @Test
    void testUserScenarioFromClass() {
        assertEquals("World",
                eggg.reflect(String.class).create("Hello World").call("substring", 6).get());
    }

    @Test
    void testUserScenarioFromBean() {
        assertEquals("World",
                eggg.reflect((Object) "Hello World").call("substring", 6).get());
    }

    @Test
    void testFullChainCreateCallSet() {
        assertEquals("Hello, I'm Tom",
                eggg.reflect(Person.class).create().setField("name", "Tom").setField("age", 25)
                        .call("hello").get());
    }

    @Test
    void testFullChainProperty() {
        Person person = eggg.reflect(Person.class).create()
                .setProperty("name", "Alice")
                .setProperty("age", 30).get();
        assertEquals("Alice", person.getName());
        assertEquals(30, person.getAge());
    }

    @Test
    void testFullChainFieldAndProperty() {
        Person person = new Person();
        eggg.reflect(person)
                .setField("name", "Field")
                .setProperty("age", 20);
        assertEquals("Field", eggg.reflect(person).property("name").get());
        assertEquals(20, (int) eggg.reflect(person).field("age").get());
    }

    @Test
    void testFullChainVoidMethodAndGetter() {
        Person person = new Person();
        eggg.reflect(person)
                .setField("name", "Tom")
                .call("voidMethod");  // void 返回 this
        assertEquals("Tom", person.getName());
    }

    @Test
    void testFullChainCalculator() {
        Calculator calc = new Calculator();
        int sum = eggg.reflect(calc).call("add", 10, 20).get();
        assertEquals(30, sum);
        long product = eggg.reflect(calc).call("multiply", 3L, 7L).get();
        assertEquals(21L, product);
    }

    // ==================== I. Eggg 实例隔离与缓存 ====================

    @Test
    void testCustomEgggInstance() {
        Eggg customEggg = new Eggg();
        assertEquals("World",
                customEggg.reflect((Object) "Hello World").call("substring", 6).get());
    }

    @Test
    void testMultipleEgggInstances() {
        Eggg eggg1 = new Eggg();
        Eggg eggg2 = new Eggg();
        assertEquals("A",
                eggg1.reflect((Object) "AB").call("substring", 0, 1).get());
        assertEquals("B",
                eggg2.reflect((Object) "AB").call("substring", 1, 2).get());
    }

    @Test
    void testCachingConsistency() {
        Person person = new Person("Tom");
        String r1 = eggg.reflect(person).call("hello").get();
        String r2 = eggg.reflect(person).call("hello").get();
        assertEquals(r1, r2);
    }

    @Test
    void testReflectAfterEgggClear() {
        Eggg eggg = new Eggg();
        String r1 = eggg.reflect((Object) "Hello").call("substring", 0, 2).get();
        eggg.clear();
        String r2 = eggg.reflect((Object) "Hello").call("substring", 0, 2).get();
        assertEquals(r1, r2);
    }

    // ==================== J. 异常场景 ====================

    @Test
    void testExceptionCauseIsNoSuchMethod() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("nonExistent"));
        assertTrue(ex.getCause() instanceof NoSuchMethodException);
    }

    @Test
    void testExceptionCauseIsNoSuchField() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).field("nonExistentField"));
        assertTrue(ex.getCause() instanceof NoSuchFieldException);
    }

    @Test
    void testExceptionMessageContainsClassName() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("nonExistent"));
        String msg = ex.getCause().getMessage();
        assertTrue(msg.contains("Person"), "Exception message should contain class name: " + msg);
    }

    @Test
    void testExceptionMessageContainsMethodName() {
        EgggReflectException ex = assertThrows(EgggReflectException.class, () ->
                eggg.reflect(new Person()).call("nonExistent"));
        String msg = ex.getCause().getMessage();
        assertTrue(msg.contains("nonExistent"), "Exception message should contain method name: " + msg);
    }

    @Test
    void testSetOnFinalField() {
        Book book = eggg.reflect(Book.class).create("Title", "Author").get();
        // final 字段 setField 静默忽略（FieldEggg.setValue 内部检查 isFinal）
        eggg.reflect(book).setField("title", "NewTitle");
        assertEquals("Title", book.getTitle()); // final 不变
    }

    @Test
    void testCallTargetMethodThrows() {
        // String.substring(-1) 触发 StringIndexOutOfBoundsException
        assertThrows(EgggReflectException.class, () ->
                eggg.reflect((Object) "Hello").call("substring", -1));
    }
}
