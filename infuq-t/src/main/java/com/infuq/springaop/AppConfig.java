package com.infuq.springaop;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(value = "com.infuq.springaop")
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class AppConfig {


}
