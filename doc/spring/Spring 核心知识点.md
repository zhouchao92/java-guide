#### BeanFactory和ApplicationContext的区别？
相同点：

1. 都是 Spring 的 IOC 容器，interface
2. 都支持XML配置属性，或属性自动注入
3. 均提供了 getBean() 获取 bean

不同点：

|                    | BeanFactory                                              | ApplicationContext                                              |
|--------------------|----------------------------------------------------------|-----------------------------------------------------------------|
| 继承关系           | public interface ListableBeanFactory extends BeanFactory | public interface ApplicationContext extends ListableBeanFactory |
| 国际化             | 支持                                                     | 不支持                                                          |
| bean对象实例化时机 | 调用 getBean() 时实例化 bean                             | 启动容器时实例化                                                |
| 核心实现           | XMLBeanFactory                                           | ClassPathXmlApplicatinoContext，增加了 getServletContext() 方法 |
| 自动注入           | 调用 API 注册AutoWiredBeanPostProcessor                  | XML 配置                                                        |
| 事件发布监听       | 不支持                                                   | 支持                                                            |

#### Spring 的事务传播机制是什么？
| **行为**     | **描述**                                                                                                       |
|--------------|----------------------------------------------------------------------------------------------------------------|
| REQUIRED     | 如果有事务在运行，当前的方法就在这个事务内运行，否则，就启动一个新的事务，并在自己的事务内运行                 |
| REQUIRED_NEW | 当前的方法必须启动新事务，并在它自己的事务内运行，如果有事务正在运行，应该将它挂起                             |
| SUPPORTS     | 如果有事务在运行，当前的方法就在这个事务内运行，否则它可以不运行在事务中                                       |
| NOT_SUPPORTS | 当前的方法不应该运行在事务中，如果有运行的事务，将它挂起                                                       |
| MANDATORY    | 当前的方法必须运行在事物内部，如果没有正在运行的事务，就抛出异常                                               |
| NEVER        | 当前的方法不应该运行在事务中，如果有运行的事务，就抛出异常                                                     |
| NESTED       | 如果有事务在运行，当前的方法就应该在这个事务的嵌套事务内运行，否则，就启动一个新的事务，并在它自己的事务内运行 |

#### 单例 Bean 是线程安全的吗？
不是，Spring 框架没有对 Bean 进行多线程的封装，如果需要可以使用原型模式的作用域 prototype

#### Spring 中使用了哪些设计模式及场景？
| 工厂模式                            | BeanFacotry 和 ApplicationContext 的创建中                                                                                 |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| 模板模式                            | BeanFacotry 和 ApplicationContext 的实现中                                                                                 |
| 代理模式                            | Spring AOP 是利用了 AspectJ AOP 实现的                                                                                     |
| 策略模式                            | 加载文件资源方式，ClassPathResource、ClassRelativeContextResource、ServletContextResource、FileSystemResource、UrlResource |
| AOP的实现：JDK动态代理和 CGLIB 代理 |                                                                                                                            |
| 单例模式                            | Bean 创建                                                                                                                  |
| 观察者模式                          | ApplicationEvent，ApplicationListener，ApplicationEventPublisher                                                           |
| 适配器模式                          | MethodBeforeAdviceAdapter，ThrowsAdviceAdapter，AfterReturningAdapter                                                      |
| 装饰者模式                          | 源码中 Wrapper 和 Decorator 后缀的                                                                                         |

#### Spring 事务的实现原理是什么？

- 声明式事务：@Transactional 注解，给方法所在的类生成动态代理对象做为 bean，在业务执行出现异常时，回滚，否则提交事务
- 编程式事务

#### Spring 事务什么情况下会失效？

- bean 对象没有被容器管理
- 方法的访问修饰符不是 public
- 自身调用问题，本类内调用方法不会走 AOP
- 数据源没有配置事务管理器
- 数据库不支持事务
- 异常被捕获
- 异常类型错误或配置错误 RollbackForClass
#### Bean对象的作用域有哪些？

- singleton
- prototype
- request
- session
- global-session

#### 简述 Bean 对象的生命周期

1. 实例化 Bean 对象，通过反射的方式创建对象，只是在堆内存中申请空间，属性为默认值
2. set 设置对象属性
3. 检查 Aware 相关接口并设置相关依赖，如果对象中需要引用容器内部的对象，需要调用 Aware 接口的子类方法进行统一的设置
4. BeanPostProcessor 的前置处理（postProcessBeforeInitialization）
5. 检查是否是 InitializingBean 的子类来决定是否调用 afterPropertiesSet 方法
6. 检查是否配置有自定义的 init-method 方法，初始化
7. BeanPostProcessor 的后置处理（postProcessAfterInitialization）
8. 当 Bean 不再需要时，会经过清理阶段，如果 Bean 实现了 DisposableBean 接口，会调用其实现的 destroy 方法
9. 如果这个 Bean 的 Spring 配置中配置了destroy-method 属性，会自动调用其配置的销毁方法

#### 如何实现一个 IOC 容器？

1. 准备基本的容器对象，包含一些 map 结构的集合，用于存储具体的对象
2. 进行配置文件读取或注解解析，将需要创建的 bean 对象都封装成 BeanDefinition 对象存储在容器中
3. 将封装好的 BeanDefinition 对象实例化
4. 初始化对象，设置对应的属性，依赖注入，完成整个对象的创建并放入 map 中存储
5. 通过容器获取对象，对象获取与逻辑处理
6. 提供对象的销毁，当对象不在使用时或容器关闭时，将无用的对象销毁

#### 在配置文件中Bean的自动装配方式有哪些？

- no 缺省，通过'ref' 属性手动设定
- byName
- byType
- constructor 构造器参数的 byType
- autodetect 自动检测，如果找到默认的构造函数，用构造，否则按照类型自动装配

#### Spring 如何选择是用 JDK 还是 CGLIB ？

1. 当 bean 实现接口时，会用 JDK 代理模式
2. 当 bean 没有实现接口，用 CGLIB 实现
3. 可以强制使用 CGLIB（在 Spring 配置中加入 `<aop:aspectj-autoproxy proxyt-target-class="true"/>`）

#### Spring 怎么解决循环依赖？
三级缓存
_* 第一层缓存：singletonObjects 单例池，已初始化好的单例对象（代理对象）
* 第二层缓存：earlySingletonObjects 提前产生的代理对象（未经过完整生命周期的普通对象/代理对象）
* 第三层缓存：singletonFactories 打破循环，lambda表达式 aop创建代理对象，实际存储普通对象，以便于后面扩展有机会创建代理对象（_用于保存bean创建工厂_）_

![Spring 三级缓存原理](/pic/Spring%20三级缓存原理.png)

_出现循环依赖现象==>提前AOP，属性注入，注入的代理对象  代理对象.target=普通对象_

三级缓存解决不了构造器中循环依赖问题，可以通过@Lazy注解解决（_先生成属性基于Lazy的代理对象，在后期调用或其他时机再真正从单例池中获取_）

#### Spring Bean 创建策略
![Spring Bean 创建策略](/pic/Spring%20Bean%20创建策略.png)
