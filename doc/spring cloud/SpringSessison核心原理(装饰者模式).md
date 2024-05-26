_@EnableRedisHttpSession
1. 导入 @RedisHttpSessionConfiguration 配置
1.1. 给容器添加了一个组件 RedisIndexedSessionRepository===>_RedisOperationsSessionRepository==>_SessionRepository，redis 操作 session 的增删改查包装类
1.2. SessionRepositoryFilter ==>Filter，session 存储过滤器，每次请求都经过此过滤器
1.2.1. 创建时，自动从容器中获取到 SessionRepository_
_1.2.2.包装原始的请求和响应对象_
_1.2.3._wrappedRequest.getSession() ==> _SessionRepository_
```java
public class SessionRepositoryFilter<S extends Session> extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);

        // 包装原始请求对象
		SessionRepositoryRequestWrapper wrappedRequest = new SessionRepositoryRequestWrapper(request, response);
		// 包装原始响应对象
        SessionRepositoryResponseWrapper wrappedResponse = new SessionRepositoryResponseWrapper(wrappedRequest,
				response);

		try {
            // 包装后的对象应用到后续的执行链中
			filterChain.doFilter(wrappedRequest, wrappedResponse);
		}
		finally {
			wrappedRequest.commitSession();
		}
	}
}
```
