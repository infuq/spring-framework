package com.infuq.sharding;

import com.alibaba.druid.pool.DruidDataSource;
import com.infuq.sharding.service.UserService;
import com.infuq.sharding.service.UserServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

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
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
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
