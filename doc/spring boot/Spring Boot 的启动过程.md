准备阶段 + 启动过程
```java
new SpringApplication(primarySources).run(args);
```

准备阶段：

1. 推断应用类型，SERVLET，根据 `String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet", "org.springframework.web.context.ConfigurableWebApplicationContext" };`
2. （初始化器）加载 META-INF/spring.factories 下 `org.springframework.context.ApplicationContextInitializer`，以反射的形式创建实例
3. （监听器）加载 META-INF/spring.factories 下 `org.springframework.context.ApplicationListener` ===》事件监听器，以反射的形式创建实例
4. 根据调用的栈推断主类类名


启动过程：

1. 获取并启动监听器
2. 构建应用上下文对象
3. 初始化应用上下文
4. 刷新上下文的准备阶段
5. 刷新应用上下文
6. 刷新应用上下文的扩展接口【方便后续拓展】


```java
public ConfigurableApplicationContext run(String... args) {
  StopWatch stopWatch = new StopWatch();
  stopWatch.start();
  ConfigurableApplicationContext context = null;
  Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
  configureHeadlessProperty();

  // 1.获取并启动监听器
  // 加载 META-INF/spring.factories 下 org.springframework.boot.SpringApplicationRunListener
  SpringApplicationRunListeners listeners = getRunListeners(args);

  // org.springframework.boot.context.event.EventPublishingRunListener
  listeners.starting();
  try {
    ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

    // 2.构建应用上下文对象
    // org.springframework.web.context.support.StandardServletEnvironment
    // 加载用户配置，多环境下的配置文件
    // org.springframework.boot.context.config.ConfigFileApplicationListener
    ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);

    // 处理需要忽略的 bean
    configureIgnoreBeanInfo(environment);

    // 打印 banner
    Banner printedBanner = printBanner(environment);

    // 3.初始化应用上下文
    /*
      实例化对象
      过程中会创建 IOC 容器 beanFactory ，对象继承于 GenericWebApplicationContext
      {@link org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext}
      */
    context = createApplicationContext();

    // 报告启动过程中发生的错误
    // org.springframework.boot.SpringBootExceptionReporter
    exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
        new Class[] { ConfigurableApplicationContext.class }, context);

    // 4.刷新上下文的准备阶段
    // 设置 environment ==> 执行容器后置处理 ==> 应用 initializers ==> 向监听器发送容器已准备好的事件 SpringApplicationRunListener
    // bean 对象的创建：将main函数中的args封装成单例bean放在容器中 springApplicationArguments，将banner也放在容器中 springBootBanner
    // 将主类注入到容器中 org.springframework.boot.SpringApplication.load
    prepareContext(context, environment, listeners, applicationArguments, printedBanner);

    // 5.刷新应用上下文
    // 注册 bean 的方法 invokeBeanFactoryPostProcessors
    // resource 定位，一般有三种方式：主类所在的包，SPI 扩展机制实现的自动装配 starter，@Import导入
    // BeanDefinition 载入
    // 注册 BeanDefinition ==> DefaultListableBeanFactory.beanDefinitionMap
    // componentScan 扫描，@Import扫描

    // 启动 Tomcat，将 DispatcherServlet注册到servletContext中
    refreshContext(context);

    // 6.刷新应用上下文的扩展接口
    afterRefresh(context, applicationArguments);

    stopWatch.stop();
    if (this.logStartupInfo) {
      new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
    }
    listeners.started(context);
    callRunners(context, applicationArguments);
  }
  catch (Throwable ex) {
    handleRunFailure(context, ex, exceptionReporters, listeners);
    throw new IllegalStateException(ex);
  }

  try {
    listeners.running(context);
  }
  catch (Throwable ex) {
    handleRunFailure(context, ex, exceptionReporters, null);
    throw new IllegalStateException(ex);
  }
  return context;
}
```