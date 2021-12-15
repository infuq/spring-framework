package com.infuq.springframe;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Example {


    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

        QueryComputerService bean = context.getBean(QueryComputerService.class);

        int x = bean.queryComputerCount();

		System.out.println("num -> " + x);
    }



}
