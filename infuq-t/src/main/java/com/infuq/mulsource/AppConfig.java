package com.infuq.mulsource;

import com.infuq.mulsource.service.UserService;
import com.infuq.mulsource.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement
@ComponentScan
public class AppConfig {

	@Bean
	public UserService userService() {
		return new UserServiceImpl();
	}


}
