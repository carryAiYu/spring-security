
## 自定义短信验证码登录

[`SmsCodeAuthenticationSecurityConfig`](../src/main/java/org/yuan/sms/config/SmsCodeAuthenticationSecurityConfig.java) 验证码登录配置

```
@Configuration
public class SmsCodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		// 自定义SmsCodeAuthenticationFilter过滤器
		SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
		smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		smsCodeAuthenticationFilter.setAuthenticationFailureHandler(new SmsAuthenticationFailureHandler());
		smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(new SmsAuthenticationSuccessHandler());

		// 设置自定义SmsCodeAuthenticationProvider的认证器userDetailsService
		SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
		smsCodeAuthenticationProvider.setUserDetailsService(new SmsDetailsService());
		// 在UsernamePasswordAuthenticationFilter过滤前执行
		http.authenticationProvider(smsCodeAuthenticationProvider).addFilterAfter(
				smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
```
