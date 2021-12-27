package com.infuq.mulsource;

import com.infuq.mulsource.config.PrimaryDataSourceConfig;
import com.infuq.mulsource.config.SecondDataSourceConfig;
import com.infuq.mulsource.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class Main {


	private static final Log logger = LogFactory.getLog(Main.class);


    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class,
				PrimaryDataSourceConfig.class,
				SecondDataSourceConfig.class);


        applicationContext.getBean(UserService.class).getList();

//        System.in.read();

    }


}
