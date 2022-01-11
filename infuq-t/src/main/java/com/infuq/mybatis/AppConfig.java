package com.infuq.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.infuq.mybatis.service.UserService;
import com.infuq.mybatis.service.UserServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

@EnableTransactionManagement
@MapperScan("com.infuq.mybatis.mapper")
@ComponentScan
public class AppConfig {


    @Bean
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test_0?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setUsername("root");
        dataSource.setPassword("9527");
		dataSource.setInitialSize(1);
		dataSource.setMinIdle(0);
		dataSource.setMaxActive(1);
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
        dataSource.setUrl("jdbc:mysql://localhost:3306/test_1?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setUsername("root");
        dataSource.setPassword("9527");

        return dataSource;
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource druidDataSource) throws Exception {

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(druidDataSource);
        return factoryBean.getObject();

    }

    @Bean
    public DataSourceTransactionManager druidTransactionManager(DataSource druidDataSource) {
        return new DataSourceTransactionManager(druidDataSource);
    }


    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }


}
