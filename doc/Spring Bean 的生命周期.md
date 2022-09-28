Spring Bean 的生命周期
1. 解析类得到 BeanDefinition
2. 推断构造方法
3. 确定构造方法，反射的方式实例化对象
4. 属性注入（@Autowired）
5. 回调 Aware 接口，BeanNameAware、BeanFactoryAware
6. BeanPostProcessor 前置处理 postProcessBeforeInitialization()
7. 初始化，InitialingBean 的 afterProperties()
8. BeanPostProcessor 后置处理 postProcessAfterInitialization()
9. 单例对象-->放入单例池
10. use bean
11. DisposableBean 的 destroy() 方法