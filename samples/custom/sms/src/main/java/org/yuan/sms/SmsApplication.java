package org.yuan.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class, args);
	}
	private volatile static Map<String, String> cache = new ConcurrentHashMap<>(16);

	public static Map<String, String> getCache() {
		return cache;
	}
}
