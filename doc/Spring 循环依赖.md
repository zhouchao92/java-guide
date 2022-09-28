Spring 是如何解决循环依赖问题？
在 Spring 中，会通过多种方式解决循环依赖问题
1. 三级缓存，解决属性注入方式的循环依赖问题
2. @Lazy，解决构造方法的循环依赖问题（在创建对象时先往ioc容器中放一个属性为空的对象）【要求：必须有空的构造函数创建对象】


为什么需要三级缓存来解决循环依赖问题？
二级缓存能解决普通对象的循环依赖问题
三级缓存是针对引用的对象配置了 Aop，在 ioc 中最终需要放的是代理对象，生成动态代理是在对象初始化完成之后【三级缓存保存动态代理的配置信息，发现循环依赖，提前 aop】

```java
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