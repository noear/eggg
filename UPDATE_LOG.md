
### 1.0.11

* 添加 ConstrEggg: getParamEgggBy, hasParamEgggBy 方法
* 添加 MethodEggg: getParamEgggBy, hasParamEgggBy 方法

### 1.0.10

* 修复 ClassEggg:loadMethods 出现父类的同名属方法会盖掉当前类的 propertyEgggsForName 值


### 1.0.9

* 修复 ClassEggg:loadFields 出现父类的同名私有字段盖掉当前类的 propertyEgggsForName 值

### 1.0.8

* 添加 TypeEggg:isCollection, isSet 方法
* 修复 ClassEggg:loadFields 出现父类的私有字段盖掉当前类的字段

### 1.0.7

* 取消 WildcardTypeImpl 的 clone 处理（不利于缓存）

### 1.0.6

* 添加 TypeEggg:isJdkType() 方法（是否为 jdk 提供的类型）

### 1.0.5

* 添加 ConstrEggg:isStatic() 方法
* 修复 ConstrEggg 当为静态函数时不能执行的问题

### 1.0.4

* 优化 newTypeEggg, newClassEggg 异常提示（可显示触发异常的类）

### 1.0.3

* 优化 createTypeSelfGenericMap 和方法参数的泛型传递

### 1.0.2

* 添加 TypeEggg:getOriginType 方法（获取原始类型）
* 简化 TypeEggg 初始构造
* 优化 List,Map 修改安全
* 优化 Creator 识别机制，增加表过式匹配（之前只能是注解）
* 优化 eggg.findGenericInfo，增加 Interface 的情况
* 优化 泛型传递策略（浅度查找，深度钻入）


### 1.0.1

* 优化 AnnotatedEggg 接口
* 优化 AliasHandler 与 DigestHandler 接口

