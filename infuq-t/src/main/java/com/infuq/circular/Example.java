package com.infuq.circular;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class Example {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        QueryMemoryService bean = context.getBean(QueryMemoryService.class);
        bean.queryMemoryCount();

    }



}
