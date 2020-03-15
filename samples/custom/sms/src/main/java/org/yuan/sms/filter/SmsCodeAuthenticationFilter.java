package org.yuan.sms.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.yuan.sms.authentication.SmsCodeAuthenticationToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	/**
	 * 手机号参数名称
	 */
	private String mobileParameter = "mobile";
	/**
	 * 验证码参数名称
	 */
	private String validateCodeParameter = "validateCode";
	/**
	 * post请求
	 */
	private boolean postOnly = true;

	public SmsCodeAuthenticationFilter() {
		// 处理的手机验证码登录请求处理url
		super(new AntPathRequestMatcher("/authentication/mobile", "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
		// 判断是是不是post请求
		if (postOnly && !request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("验证请求方法不支持: " + request.getMethod());
		}

		BufferedReader streamReader = new BufferedReader( new InputStreamReader(request.getInputStream(), "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		ObjectMapper mapper = new ObjectMapper();

		Map<String ,String> map = mapper.readValue(responseStrBuilder.toString(), Map.class);

		// 从请求中获取手机号码
		String mobile = map.get(mobileParameter);
		// 获取验证码
		String validateCode = map.get(validateCodeParameter);

		if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(validateCode)) {
			throw new InternalAuthenticationServiceException("验证参数异常");
		}

		mobile = mobile.trim();
		validateCode = validateCode.trim();
		//创建SmsCodeAuthenticationToken(未认证)
		SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(mobile, validateCode);

		// 设置额外用户信息
		setDetails(request, authRequest);
		// 返回Authentication实例
		return this.getAuthenticationManager().authenticate(authRequest);
	}

	protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	public void setMobileParameter(String mobileParameter) {
		Assert.hasText(mobileParameter, "Username parameter must not be empty or null");
		this.mobileParameter = mobileParameter;
	}

	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

	public final String getMobileParameter() {
		return mobileParameter;
	}
}
