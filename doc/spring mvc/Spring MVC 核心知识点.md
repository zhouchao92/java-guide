#### Spring MVC 的工作流程是什么？
1. 用户发送请求至前端控制器 DispatcherServlet
2. DispatcherServlet 收到请求调用 HandlerMapping 处理器映射器。
3. 处理器映射器找到具体的处理器，生成处理器对象及处理器拦截器(如果有则生成)一并返回给 DispatcherServlet。
4. DispatcherServlet 调用 HandlerAdapter 处理器适配器
5. HandlerAdapter 经过适配调用具体的处理器(Controller，也叫后端控制器)。
6. Controller 执行完成返回 ModelAndView
7. HandlerAdapter 将 Controller 执行结果 ModelAndView 返回给 DispatcherServlet
8. DispatcherServlet 将 ModelAndView 传给 ViewReslover 视图解析器
9. ViewReslover 解析后返回具体 View
10. DispatcherServlet 根据 View 进行渲染视图（即将模型数据填充至视图中）。
11. DispatcherServlet 响应用户

![Spring MVC 工作流程](/pic/Spring%20MVC%20工作流程.png)

#### Spring MVC的九大组件是什么？
1. MultipartResolver（文件处理器）
对应的初始化方法是 initMultipartResolver(context)，用于处理上传请求。
处理方法是将普通的 request 包装成 MultipartHttpServletRequest，后者可以直接调用 getFile 方法获取 File。
2. LocaleResolver（当前环境处理器）
对应的初始化方法是 initLocaleResolver(context)，这就相当于配置数据库的方言一样，有了这个就可以对不同区域的用户显示不同的结果。
SpringMVC 主要有两个地方用到了 Locale：
一是 ViewResolver 视图解析的时候；
二是用到国际化资源或者主题的时候。
3. ThemeResolver（主题处理器）
对应的初始化方法是 initThemeResolver(context)，用于解析主题。
SpringMVC 中一个主题对应一个 properties 文件，里面存放着跟当前主题相关的所有资源，如图片、css 样式等。
SpringMVC 的主题也支持国际化。
4. HandlerMapping（处理器映射器）
对应的初始化方法是initHandlerMappings(context)，这就是根据用户请求的资源uri来查找Handler的。
在SpringMVC中会有很多请求，每个请求都需要一个Handler处理，具体接收到一个请求之后使用哪个Handler进行处理呢？这就是HandlerMapping需要做的事。
5. HandlerAdapters（处理器适配器）
对应的初始化方法是 initHandlerAdapters(context)，从名字上看，它就是一个适配器。
Servlet 需要的处理方法的结构却是固定的，都是以 request 和 response 为参数的方法。
如何让固定的 Servlet 处理方法调用灵活的 Handler 来进行处理呢？这就是 HandlerAdapters 要做的事情。
6. HandlerExceptionResolvers（异常处理器）
对应的初始化方法是 initHandlerExceptionResolvers(context)，其它组件都是用来干活的。
在干活的过程中难免会出现问题，出问题后怎么办呢？这就要有一个专门的角色对异常情况进行处理，在 SpringMVC 中就是 HandlerExceptionResolver。
7. RequestToViewNameTranslator（视图名称翻译器），
对应的初始化方法是 initRequestToViewNameTranslator(context)，有的 Handler 处理完后并没有设置 View 也没有设置ViewName，这时就需要从 request 获取 ViewName 了，如何从 request 中获取 ViewName 就是 RequestToViewNameTranslator 要做的事情了。
8. ViewResolvers（页面渲染处理器）
对应的初始化方法是 initViewResolvers(context)，ViewResolver 用来将 String 类型的视图名和 Locale 解析为 View 类型的视图。
View 是用来渲染页面的，也就是将程序返回的参数填入模板里，生成 html（也可能是其它类型）文件。
9. FlashMapManager（参数传递管理器）
对应的初始化方法是 initFlashMapManager(context)，用来管理 FlashMap 的，FlashMap 主要用在 redirect 重定向中传递参数。
