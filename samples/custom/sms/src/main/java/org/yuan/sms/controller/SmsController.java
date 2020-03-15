package org.yuan.sms.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yuan.sms.SmsApplication;
import org.yuan.sms.dto.SmsDTO;

@RestController
public class SmsController {

	@RequestMapping(value = "code/sms", method = RequestMethod.POST)
	public String getValidateCode(@RequestBody SmsDTO smsDTO) {
		// 把验证码存入缓存 实际生产环境可以使用redis并设置过期时间
		SmsApplication.getCache().put(smsDTO.getPhone(), "12345"/* 这里就随便使用一个验证码 */);
		return "12345";
	}

}
