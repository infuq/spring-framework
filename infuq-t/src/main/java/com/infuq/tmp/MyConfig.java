package com.infuq.tmp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {

	@Bean
	private Computer computer() {
		return new Computer();
	}

	@Bean
	private Book book() {
		return new Book();
	}


}
