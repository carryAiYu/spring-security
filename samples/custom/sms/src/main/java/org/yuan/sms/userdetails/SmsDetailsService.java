package org.yuan.sms.userdetails;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmsDetailsService implements UserDetailsService {
	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		// 根据自己的业务逻辑完成从数据库或者redis获取用户信息

		return new User(s, "No-Pass",
				/* 授权信息根据业务自定义 */
				Stream.of(new SimpleGrantedAuthority("admin")).collect(Collectors.toList()));
	}
}
