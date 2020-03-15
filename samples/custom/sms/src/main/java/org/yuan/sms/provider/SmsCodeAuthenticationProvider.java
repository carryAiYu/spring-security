package org.yuan.sms.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.yuan.sms.SmsApplication;
import org.yuan.sms.authentication.SmsCodeAuthenticationToken;

public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;
		// 调用自定义的userDetailsService认证
		UserDetails user = userDetailsService.loadUserByUsername((String) authenticationToken.getPrincipal());

		if (user == null) {
			throw new InternalAuthenticationServiceException("无法获取用户信息");
		}

		String validateCode = SmsApplication.getCache().get(user.getUsername());

		if (validateCode == null) {
			throw new BadCredentialsException("验证码异常");
		}

		// 重新构建SmsCodeAuthenticationToken（已认证）
		SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(user, validateCode,
				user.getAuthorities());

		authenticationResult.setDetails(authenticationToken.getDetails());
		return authenticationResult;
	}

	/**
	 * 只有Authentication为SmsCodeAuthenticationToken使用此Provider认证
	 * @param authentication
	 * @return
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}
