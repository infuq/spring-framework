package com.infuq.mulsource.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;


@Configuration
@MapperScan(basePackages = {"com.infuq.mulsource.mapper.second"}, sqlSessionFactoryRef = "secondSqlSessionFactory")
public class SecondDataSourceConfig {


    @Bean(name="secondDataSource")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test_1?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setUsername("root");
        dataSource.setPassword("9527");

        return dataSource;
    }

    @Bean(name = "secondTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("secondDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "secondSqlSessionFactory")
    public SqlSessionFactory sessionFactory(@Qualifier("secondDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        /*
		bean.setConfiguration();
		bean.setMapperLocations();
        */
        bean.setDataSource(dataSource);

        return bean.getObject();
    }

    @Bean(name = "secondSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("secondSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}
