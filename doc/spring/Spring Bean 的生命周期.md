Spring Bean 的生命周期
1. 解析类得到 BeanDefinition
2. 推断构造方法
3. 确定构造方法，反射的方式实例化对象
4. 属性注入（@Autowired）
5. 回调 Aware 接口，BeanNameAware、BeanFactoryAware
6. BeanPostProcessor 前置处理 postProcessBeforeInitialization()
7. 初始化，InitialingBean 的 afterPropertiesSet()
8. BeanPostProcessor 后置处理 postProcessAfterInitialization()
9. 单例对象-->放入单例池
10. use bean
11. DisposableBean 的 destroy() 方法



构建 BeanDefinition   
-> 推断构造函数  
-> 实例化（先将普通对象相关信息 lambda 表达式放入三级缓存）  
-> 属性注入 【存在循环依赖问题】  
-> 执行Aware接口相关方法  
-> BeanPostProcessor 前置处理，初始化前 postProcessorBeforeInitialization()  
-> InitializingBean 初始化 afterPropertiesSet()  
-> BeanPostProcessor 后置处理， 初始化后 postProcessorAfterInitialization()  
-> 如果是单例Bean，放入单例池中  
--> 如果出现了循环依赖会执行 getBean()  
-> use bean  
-> 执行 DisposableBean 销毁 destroy()  