package com.infuq.springframe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {

	@Bean
	public Computer computer() {
		return new Computer();
	}


}
