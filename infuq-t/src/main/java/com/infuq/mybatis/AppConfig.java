package com.infuq.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.infuq.mybatis.service.UserService;
import com.infuq.mybatis.service.UserServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EnableTransactionManagement
@MapperScan("com.infuq.mybatis.mapper")
@ComponentScan
public class AppConfig {

    @Bean
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.0.102:3306/db0?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setUsername("root");
        dataSource.setPassword("9527");
		dataSource.setInitialSize(3);
		dataSource.setMinIdle(3);
		dataSource.setMaxActive(5);
		dataSource.setMinEvictableIdleTimeMillis(50 * 1000);
		dataSource.setMaxEvictableIdleTimeMillis(51 * 1000);
		dataSource.setTimeBetweenEvictionRunsMillis(15 * 1000);

		try {
			// 手动初始化连接池
			dataSource.init();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dataSource;
    }


    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.0.102:3306/db1?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setUsername("root");
        dataSource.setPassword("9527");

        return dataSource;
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource druidDataSource) throws Exception {

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(druidDataSource);
        SqlSessionFactory sqlSessionFactory = factoryBean.getObject();
        System.out.println("向Spring容器中放入SqlSessionFactory(@" + sqlSessionFactory.hashCode() + ")");
        return sqlSessionFactory;

    }

    @Primary
    @Bean
    public DataSourceTransactionManager druidTransactionManager(DataSource druidDataSource) {

        DataSourceTransactionManager druidTransactionManager = new DataSourceTransactionManager(druidDataSource);

        System.out.println("向Spring容器中放入事务管理器(@" + druidTransactionManager.hashCode() + ")");
        return druidTransactionManager;
    }


    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        return transactionManager;
    }



    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }




}
