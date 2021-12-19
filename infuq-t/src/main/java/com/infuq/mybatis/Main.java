package com.infuq.mybatis;

import com.infuq.mybatis.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


public class Main {


	private static final Log logger = LogFactory.getLog(Main.class);


    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

		DataSourceTransactionManager druidTransactionManager = (DataSourceTransactionManager) applicationContext.getBean("druidTransactionManager");

		DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) applicationContext.getBean("transactionManager");

		logger.info("事务管理器(druidTransactionManager)哈希值=" + druidTransactionManager.hashCode());
		logger.info("事务管理器(druidTransactionManager)的数据源哈希值=" + druidTransactionManager.getDataSource().hashCode());

		logger.info("事务管理器(transactionManager)哈希值=" + transactionManager.hashCode());
		logger.info("事务管理器(transactionManager)的数据源哈希值=" + transactionManager.getDataSource().hashCode());



        applicationContext.getBean(UserService.class).getList();

//        System.in.read();

    }


}
