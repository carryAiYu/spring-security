package org.yuan.sms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SmsCodeAuthenticationSecurityConfig smsAuthenticationConfig;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests()   // 验证所有请求(鉴权)
				.antMatchers("/code/sms").permitAll() // 发送验证码接口不验证
				.anyRequest() // 对于所有的请求
				.authenticated()
				.and()
				.csrf().disable()
				.apply(smsAuthenticationConfig);  // 应用验证码登录配置
	}


}
