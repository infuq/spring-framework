package com.infuq.springaop;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;



public class Example {


    public static void main(String[] args) throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

//        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        QueryComputerService bean = context.getBean(QueryComputerService.class);

        System.out.println("xxx");

        int x = bean.queryComputerCount();

		System.in.read();

    }



}
