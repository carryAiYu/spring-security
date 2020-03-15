package org.yuan.sms.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.yuan.sms.filter.SmsCodeAuthenticationFilter;
import org.yuan.sms.handler.SmsAuthenticationFailureHandler;
import org.yuan.sms.handler.SmsAuthenticationSuccessHandler;
import org.yuan.sms.provider.SmsCodeAuthenticationProvider;
import org.yuan.sms.userdetails.SmsDetailsService;

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
