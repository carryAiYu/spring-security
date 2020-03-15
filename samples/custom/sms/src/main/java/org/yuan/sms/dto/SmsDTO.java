package org.yuan.sms.dto;

import java.io.Serializable;

public class SmsDTO implements Serializable {
	private String phone;
	private String validateCode;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}
}
