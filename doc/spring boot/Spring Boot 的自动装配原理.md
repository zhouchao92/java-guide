# Spring Boot 的自动装配原理是什么？

1）通过注解@SpringBootApplication=>@EnableAutoConfiguration=>@Import({AutoConfigurationImportSelector.class})实现自动装配

2）AutoConfigurationImportSelector类中重写了ImportSelector中selectImports方法，批量返回需要装配的配置类

3）通过Spring提供的SpringFactoriesLoader机制，扫描classpath下的META-INF/spring.factories文件，读取需要自动装配的配置类

4）依据条件筛选的方式，把不符合的配置类移除掉，最终完成自动装配


## @EnableAutoConfiguration
@EnableAutoConfiguration 是 Spring Boot 核心注解 @SpringBootApplication 的组成注解其中的一个注解

```java
@AutoConfigurationPackage // 自动配置包，AutoConfigurationPackages.Registrar.class
@Import(AutoConfigurationImportSelector.class) // 导入组件
public @interface EnableAutoConfiguration {

	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	/**
	 * Exclude specific auto-configuration classes such that they will never be applied.
	 */
	Class<?>[] exclude() default {};

	/**
	 * Exclude specific auto-configuration class names such that they will never be applied.
	 */
	String[] excludeName() default {};

}
```

## @AutoConfigurationPackage
当SpringBoot应用启动时默认会将启动类所在的package作为自动配置的package

## AutoConfigurationImportSelector
核心方法 selectImports，读取 META-INF/spring.factories 文件，经过去重、过滤，返回需要装配的配置类集合
```java
@Override
public String[] selectImports(AnnotationMetadata annotationMetadata) {
    if (!isEnabled(annotationMetadata)) {
        return NO_IMPORTS;
    }
    AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
            .loadMetadata(this.beanClassLoader);
    AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,
            annotationMetadata);
    return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
}
```

加载 META-INF/spring.factories 的方法，入口 getAutoConfigurationEntry
```java
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
    List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
            getBeanClassLoader());
    Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
            + "are using a custom packaging, make sure that file is correct.");
    return configurations;
}
```

## SpringFactoriesLoader机制读取需要自动装配的配置类
- Spring Boot启动过程过程中：refresh()
- invokeBeanFactoryPostProcessors(beanFactory); // AbstractApplicationContext.java
- PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
- invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
- ```java
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}
  ```
- org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry
- processConfigBeanDefinitions(registry)
- parser.parse(candidates)
- this.deferredImportSelectorHandler.process()
- handler.processGroupImports()
- processImports(configurationClass, asSourceClass(configurationClass, exclusionFilter), Collections.singleton(asSourceClass(entry.getImportClassName(), exclusionFilter)), exclusionFilter, false)
- String[] importClassNames = selector.selectImports(currentSourceClass.getMetadata());
