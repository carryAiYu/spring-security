 
# Spring Security

Spring Security provides security services for the [Spring IO Platform](https://docs.spring.io). Spring Security 5.0 requires Spring 5.0 as
a minimum and also requires Java 8.

For a detailed list of features and access to the latest release, please visit [Spring projects](https://spring.io/projects).

# 原理

## Spring Security的核心接口

[`Authentication`](./core/src/main/java/org/springframework/security/core/Authentication.java) 将请求信息封装成Token。

[`AuthenticationProvider`](./core/src/main/java/org/springframework/security/authentication/AuthenticationProvider.java) 特定`Authentication`类型的验证执行者

[`AuthenticationManager`](./core/src/main/java/org/springframework/security/authentication/AuthenticationManager.java) 统一处理验证请求

[`UserDetailsService`](./core/src/main/java/org/springframework/security/core/userdetails/UserDetailsService.java) 获取用户数据的核心接口。

[`UserDetailsManager`](./core/src/main/java/org/springframework/security/provisioning/UserDetailsManager.java) 扩展`UserDetailsService`提供用户的创建和更新能力。

### 上述几个接口的关系
`AuthenticationManager`统一处理所有验证，内部获取到`Authtication`类型之后就分派给能处理该特定类型的`AuthenticationProvider`进行处理，    
`AuthenticationProvider`中通过`UserDetailsService`的获取用户信息完成校验。    

你想问`UserDetailsManager`去哪儿了？
```java
public interface UserDetailsManager extends UserDetailsService {
}
```
`UserDetailManager`扩展自`UserDetailsService`。 


### Spring Security中的默认实现
1. [`ProviderManager`](./core/src/main/java/org/springframework/security/authentication/ProviderManager.java) 是`AuthenticationManager`的默认实现

```
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Class<? extends Authentication> toTest = authentication.getClass();
		AuthenticationException lastException = null;
		AuthenticationException parentException = null;
		Authentication result = null;
		Authentication parentResult = null;
		boolean debug = logger.isDebugEnabled();
		// 遍历所有provider
		for (AuthenticationProvider provider : getProviders()) {
			// 使用supports方法验证它能够处理当前token
			if (!provider.supports(toTest)) {
				continue;
			}

			if (debug) {
				logger.debug("Authentication attempt using "
						+ provider.getClass().getName());
			}

			try {
				// 支持就调用authenticate方法执行认证逻辑
				result = provider.authenticate(authentication);

				if (result != null) {
					copyDetails(authentication, result);
					break;
				}
			}
			catch (AccountStatusException | InternalAuthenticationServiceException e) {
				prepareException(e, authentication);
				// SEC-546: Avoid polling additional providers if auth failure is due to
				// invalid account status
				throw e;
			} catch (AuthenticationException e) {
				lastException = e;
			}
		}
		// 如果父AuthenticationManager存在，就使用父manager进行验证
		if (result == null && parent != null) {
			// Allow the parent to try.
			try {
				result = parentResult = parent.authenticate(authentication);
			}
			catch (ProviderNotFoundException e) {
				// ignore as we will throw below if no other exception occurred prior to
				// calling parent and the parent
				// may throw ProviderNotFound even though a provider in the child already
				// handled the request
			}
			catch (AuthenticationException e) {
				lastException = parentException = e;
			}
		}

		if (result != null) {
			// 是否擦除敏感信息
			if (eraseCredentialsAfterAuthentication
					&& (result instanceof CredentialsContainer)) {
				// Authentication is complete. Remove credentials and other secret data
				// from authentication
                // 擦除凭证
				((CredentialsContainer) result).eraseCredentials();
			}

			// If the parent AuthenticationManager was attempted and successful than it will publish an AuthenticationSuccessEvent
			// This check prevents a duplicate AuthenticationSuccessEvent if the parent AuthenticationManager already published it
			if (parentResult == null) {
				eventPublisher.publishAuthenticationSuccess(result);
			}
			return result;
		}

		// Parent was null, or didn't authenticate (or throw an exception).

		if (lastException == null) {
			lastException = new ProviderNotFoundException(messages.getMessage(
					"ProviderManager.providerNotFound",
					new Object[] { toTest.getName() },
					"No AuthenticationProvider found for {0}"));
		}

		// If the parent AuthenticationManager was attempted and failed than it will publish an AbstractAuthenticationFailureEvent
		// This check prevents a duplicate AbstractAuthenticationFailureEvent if the parent AuthenticationManager already published it
		if (parentException == null) {
			prepareException(lastException, authentication);
		}

		throw lastException;
	}

```

2. [`UsernamePasswordAuthenticationToken`](./core/src/main/java/org/springframework/security/authentication/UsernamePasswordAuthenticationToken.java) 是比较常用的实现

它对应的`AuthenticationProvider`是[`AbstractUserDetailsAuthenticationProvider`](./core/src/main/java/org/springframework/security/authentication/dao/AbstractUserDetailsAuthenticationProvider.java)
`AbstractUserDetailsAuthenticationProvider`是一个抽象类，它的实现者是[`DaoAuthenticationProvider`](./core/src/main/java/org/springframework/security/authentication/dao/DaoAuthenticationProvider.java)
所以`DaoAuthenticationProvider`提供了`UsernamePasswordAuthenticationToken`的验证逻辑。
 
 
主要逻辑如下

```
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
				() -> messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only UsernamePasswordAuthenticationToken is supported"));

		// Determine username
		String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED"
				: authentication.getName();

		boolean cacheWasUsed = true;
		UserDetails user = this.userCache.getUserFromCache(username);

		if (user == null) {
			cacheWasUsed = false;

			try {
				// 获取用户信息
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			catch (UsernameNotFoundException notFound) {
				logger.debug("User '" + username + "' not found");

				if (hideUserNotFoundExceptions) {
					throw new BadCredentialsException(messages.getMessage(
							"AbstractUserDetailsAuthenticationProvider.badCredentials",
							"Bad credentials"));
				}
				else {
					throw notFound;
				}
			}

			Assert.notNull(user,
					"retrieveUser returned null - a violation of the interface contract");
		}

		try {
			// 预检查 判断用户是否过期、锁定、账户冻结
			preAuthenticationChecks.check(user);
			// 子类自定义一些额外检查
			additionalAuthenticationChecks(user,
					(UsernamePasswordAuthenticationToken) authentication);
		}
		catch (AuthenticationException exception) {
			if (cacheWasUsed) {
				// There was a problem, so try again after checking
				// we're using latest data (i.e. not from the cache)
				cacheWasUsed = false;
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
				preAuthenticationChecks.check(user);
				additionalAuthenticationChecks(user,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			else {
				throw exception;
			}
		}
		// 检查密码是否过期
		postAuthenticationChecks.check(user);

		if (!cacheWasUsed) {
			this.userCache.putUserInCache(user);
		}

		Object principalToReturn = user;

		if (forcePrincipalAsString) {
			principalToReturn = user.getUsername();
		}

		return createSuccessAuthentication(principalToReturn, authentication, user);
	}
```

3. [`JdbcUserDetailsManager`](./core/src/main/java/org/springframework/security/provisioning/JdbcUserDetailsManager.java) 
提供了基于JDBC的用户的crud方法



了解了上面的流程我们就能方便的对Spring Security进行扩展
[短信验证码登录扩展](./samples/custom/sms/README.md)


## Spring Security过滤器链

[`FilterChainProxy`](./web/src/main/java/org/springframework/security/web/FilterChainProxy.java) 代理所有Spring管理的filter






## 自动配置

[`WebSecurityConfiguration`](./config/src/main/java/org/springframework/security/config/annotation/web/configuration/WebSecurityConfiguration.java) 核心配置类

```
@Autowired(required = false)
	public void setFilterChainProxySecurityConfigurer(
			ObjectPostProcessor<Object> objectPostProcessor,
			// 自动注入SecurityConfigurer 如果存在的话
			// 我们配置只需要继承SecurityConfigurer就可以了
			@Value("#{@autowiredWebSecurityConfigurersIgnoreParents.getWebSecurityConfigurers()}") List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers)
			throws Exception {
		webSecurity = objectPostProcessor
				.postProcess(new WebSecurity(objectPostProcessor));
		if (debugEnabled != null) {
			webSecurity.debug(debugEnabled);
		}
 
 		webSecurityConfigurers.sort(AnnotationAwareOrderComparator.INSTANCE);

		Integer previousOrder = null;
		Object previousConfig = null;
		for (SecurityConfigurer<Filter, WebSecurity> config : webSecurityConfigurers) {
			Integer order = AnnotationAwareOrderComparator.lookupOrder(config);
			if (previousOrder != null && previousOrder.equals(order)) {
				throw new IllegalStateException(
						"@Order on WebSecurityConfigurers must be unique. Order of "
								+ order + " was already used on " + previousConfig + ", so it cannot be used on "
								+ config + " too.");
			}
			previousOrder = order;
			previousConfig = config;
		}
		// 依次应用SecurityConfigurer
		for (SecurityConfigurer<Filter, WebSecurity> webSecurityConfigurer : webSecurityConfigurers) {
			webSecurity.apply(webSecurityConfigurer);
		}
		this.webSecurityConfigurers = webSecurityConfigurers;
	}
```

[`WebSecurity`](./config/src/main/java/org/springframework/security/config/annotation/web/builders/WebSecurity.java) 




# License
Spring Security is Open Source software released under the
https://www.apache.org/licenses/LICENSE-2.0.html[Apache 2.0 license].
