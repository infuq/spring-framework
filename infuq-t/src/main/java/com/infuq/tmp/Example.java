package com.infuq.tmp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Example {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext(MyConfig.class);

		Book bean = context.getBean(Book.class);
		System.out.println(bean);


	}
}
