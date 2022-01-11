package com.infuq.mybatis;

import com.infuq.mybatis.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.Random;


public class Main {


	private static final Log logger = LogFactory.getLog(Main.class);


    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

//		DataSourceTransactionManager druidTransactionManager = (DataSourceTransactionManager) applicationContext.getBean("druidTransactionManager");

//		DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) applicationContext.getBean("transactionManager");

//		System.out.println("事务管理器(druidTransactionManager)@" + Integer.toHexString(druidTransactionManager.hashCode()));
//		System.out.println("事务管理器(druidTransactionManager)的数据源@" + Integer.toHexString(druidTransactionManager.getDataSource().hashCode()));

//		System.out.println("事务管理器(transactionManager)@" + Integer.toHexString(transactionManager.hashCode()));
//		System.out.println("事务管理器(transactionManager)的数据源@" + Integer.toHexString(transactionManager.getDataSource().hashCode()));


		applicationContext.getBean(UserService.class).getList();


		/*
		while (true) {
			for (int i = 0; i < 15; i++) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							applicationContext.getBean(UserService.class).getList();
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
				}).start();
			}

			Thread.sleep(2000);
		}
		*/


//        System.in.read();

    }


}
