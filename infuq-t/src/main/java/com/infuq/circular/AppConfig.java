package com.infuq.circular;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(value = "com.infuq.circular")
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class AppConfig {


}
