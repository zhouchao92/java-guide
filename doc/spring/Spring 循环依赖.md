## Spring 是如何解决循环依赖问题？
在 Spring 中，会通过多种方式解决循环依赖问题
1. 三级缓存，解决属性注入方式的循环依赖问题
2. @Lazy，解决构造方法的循环依赖问题（在创建对象时先往ioc容器中放一个属性为空的对象）【要求：必须有空的构造函数创建对象】

三级缓存及其作用：
- 第一层缓存：singletonObjects 【单例】单例池，已初始化好的单例对象（代理对象）
- 第二层缓存：earlySingletonObjects 【单例】提前产生的代理对象（未经过完整生命周期的普通对象/代理对象），保证 bean 对象的单例特性，提高性能
- 第三层缓存：singletonFactories 【打破循环】，lambda表达式 aop创建代理对象，实际存储普通对象


## 为什么需要三级缓存来解决循环依赖问题？
二级缓存能解决普通对象的循环依赖问题
三级缓存是针对引用的对象配置了 Aop，在 ioc 中最终需要放的是代理对象，生成动态代理是在对象初始化完成之后【三级缓存保存动态代理的配置信息，发现循环依赖，提前 aop】

```java
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry

/** Cache of singleton objects: bean name to bean instance. */
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

/** Cache of singleton factories: bean name to ObjectFactory. */
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

/** Cache of early singleton objects: bean name to bean instance. */
private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);


@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    // Quick check for existing instance without full singleton lock
    Object singletonObject = this.singletonObjects.get(beanName);
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        singletonObject = this.earlySingletonObjects.get(beanName);
        if (singletonObject == null && allowEarlyReference) {
            synchronized (this.singletonObjects) {
                // Consistent creation of early reference within full singleton lock
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    singletonObject = this.earlySingletonObjects.get(beanName);
                    if (singletonObject == null) {
                        ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                        if (singletonFactory != null) {
                            singletonObject = singletonFactory.getObject();
                            this.earlySingletonObjects.put(beanName, singletonObject);
                            this.singletonFactories.remove(beanName);
                        }
                    }
                }
            }
        }
    }
    return singletonObject;
}
```

## 怎么避免重复AOP？
在放入三级缓存 `AbstractAutoProxyCreator#getEarlyBeanReference(java.lang.Object, java.lang.String)`，会执行 `this.earlyProxyReferences.put(cacheKey, bean);`放入缓存中，真正执行AOP在后置处理器会判断缓存中是否有这个bean

```java
if (this.earlyProxyReferences.remove(cacheKey) != bean) {
    return this.wrapIfNecessary(bean, beanName, cacheKey);
}
```

## 循环依赖---类方法标注@Aysnc的被代理对象
为什么会出现循环依赖，两次AOP，一次是初始化后@Async注解进行AOP，另一次是Aspect切面进行AOP，两次拿到的代理对象不一致  
---> 解决方案：@Lazy懒加载