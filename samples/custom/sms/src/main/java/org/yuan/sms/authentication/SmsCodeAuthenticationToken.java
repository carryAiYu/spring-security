package org.yuan.sms.authentication;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 * 手机号
	 */
	private final Object principal;

	/**
	 * 验证码
	 */
	private final Object credentials;

	/**
	 * SmsCodeAuthenticationFilter中构建的未认证的Authentication
	 * @param mobile 手机号
	 * @param credentials 验证码
	 */
	public SmsCodeAuthenticationToken(String mobile, Object credentials) {
		super(null);
		this.principal = mobile;
		this.credentials = credentials;
		setAuthenticated(false);
	}

	/**
	 * SmsCodeAuthenticationProvider中构建已认证的Authentication
	 * @param principal 手机号
	 * @param credentials 验证码
	 * @param authorities
	 */
	public SmsCodeAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		super.setAuthenticated(true); // must use super, as we override
	}

	@Override
	public Object getCredentials() {
		return this.credentials;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	/**
	 * @param isAuthenticated
	 * @throws IllegalArgumentException
	 */
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		}

		super.setAuthenticated(false);
	}

	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
	}
}
