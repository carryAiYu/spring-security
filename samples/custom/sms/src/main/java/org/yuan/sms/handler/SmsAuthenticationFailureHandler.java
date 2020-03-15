package org.yuan.sms.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmsAuthenticationFailureHandler implements AuthenticationFailureHandler {
	private ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
		Map<String, Object> map = new HashMap<>();

		if(e instanceof InternalAuthenticationServiceException) {
			map.put("code", -2);
			map.put("msg", "内部认证异常");
		} else if(e instanceof BadCredentialsException) {
			map.put("code", -1);
			map.put("msg", e.getMessage());
		} else {
			map.put("code", -3);
			map.put("msg", e.getMessage());
		}
		response.setContentType("application/json;charset=utf-8");
		response.getWriter().write(objectMapper.writeValueAsString(map));  //返回json数据
	}
}
