package com.infuq.multxinterceptor;

import com.alibaba.druid.pool.DruidDataSource;
import com.infuq.multxinterceptor.service.UserService;
import com.infuq.multxinterceptor.service.UserServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
@MapperScan("com.infuq.multxinterceptor.mapper")
@ComponentScan
public class AppConfig {


    @Bean
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test_0?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
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


	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanFactoryTransactionAttributeSourceAdvisor myTransactionAdvisor(
			TransactionAttributeSource txSource,
			TransactionInterceptor txAdvice) {

		BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		advisor.setTransactionAttributeSource(txSource);
		advisor.setAdvice(txAdvice);

		return advisor;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionAttributeSource txSource() {
		NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();

		RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
		readOnlyTx.setReadOnly(true);
		readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

		RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
		requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
		requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		requiredTx.setTimeout(300);

		Map<String, TransactionAttribute> txMap = new HashMap<>();
		txMap.put("add*", 		requiredTx);
		txMap.put("save*", 		requiredTx);
		txMap.put("insert*", 	requiredTx);
		txMap.put("update*", 	requiredTx);
		txMap.put("delete*", 	requiredTx);
		txMap.put("tmp*", 		readOnlyTx);
		txMap.put("find*", 		readOnlyTx);
		source.setNameMap(txMap);

		return source;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionInterceptor txAdvice(DataSourceTransactionManager druidTransactionManager, TransactionAttributeSource txSource) {
		return new TransactionInterceptor(druidTransactionManager, txSource);
	}




}
