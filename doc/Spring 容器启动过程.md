0. 初始化 reader、scanner
1. 将主类下转换成 BeanDefinition 注册到 ioc 容器的 beanDefinitionMap 中
2. prepareRefresh 刷新上下文的预处理
3. obtainFreshBeanFactory 获取容器初始化创建的 BeanFactory
4. prepareBeanFactory BeanFactory 预处理，往容器中添加组件
5. postProcessBeanFactory 子类重写
6. invokeBeanFactoryPostProcessors 在 BeanFactory 初始化后调用 BeanFactory 后置处理
7. registerBeanPostProcessors 向容器中注册 bean 的后置处理器（干预 Spring 初始化 bean 的流程，完成 aop 代理，自动注入、循环依赖等功能）
8. initMessageSource 初始化 MessageSource 组件（国际化）
9. initApplicationEventMulticaster 初始化事件分发器
10. onRefresh 子类重写
11. registerListeners 注册监听器
12. finishBeanFactoryInitialization 完成 BeanFactory 的初始化，初始化所有剩下的单例 bean
13. finishRefresh 发送完成上下文刷新事件
