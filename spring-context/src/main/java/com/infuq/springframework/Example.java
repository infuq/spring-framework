package com.infuq.springframework;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Example {

	public static void main(String[] args) {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("beans.xml");

		ZAnimal animal = applicationContext.getBean(ZAnimal.class);
		animal.getColor();

	}

}
