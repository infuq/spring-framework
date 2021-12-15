package com.infuq;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Example {

	public static void main(String[] args) {
		// ./gradlew clean && ./gradlew build -x test

		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml", "applicationContext.xml");

		Computer computer = context.getBean(Computer.class);

		System.out.println(computer);

	}


}
