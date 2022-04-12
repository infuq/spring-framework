package com.infuq.sharding;

import com.alibaba.druid.pool.DruidDataSource;
import com.infuq.sharding.algorithm.DatabaseShardingAlgorithm;
import com.infuq.sharding.algorithm.TableComplexKeysShardingAlgorithm;
import com.infuq.sharding.algorithm.TableShardingAlgorithm;
import com.infuq.sharding.service.UserService;
import com.infuq.sharding.service.UserServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;



@EnableTransactionManagement
@MapperScan("com.infuq.sharding.mapper")
@ComponentScan
public class ShardingConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource shardingDataSource) throws Exception {

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(shardingDataSource);
        return factoryBean.getObject();

    }

    @Bean
    public DataSource shardingDataSource() throws SQLException {


        //
        ShardingRuleConfiguration shardingRuleConfig = getShardingRuleConfiguration();

        //
        List<TableRuleConfiguration> allTableRuleConfiguration = getAllTableRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().addAll(allTableRuleConfiguration);

        //
        return ShardingDataSourceFactory.createDataSource(getDatasourceMap(), shardingRuleConfig, getProperties());
    }


    private List<TableRuleConfiguration> getAllTableRuleConfiguration() {
        List<TableRuleConfiguration> list = new ArrayList<>(8);

//        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("t", "default.t_1, default.t_2, ${['db1', 'db2']}.t_${['1', '2']}");
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration("t", "${['db0', 'db1', 'db2', 'db3']}.t_${['0', '1']}");

        // 库
        StandardShardingStrategyConfiguration databaseShardingStrategyConfig = new StandardShardingStrategyConfiguration("u_id", new DatabaseShardingAlgorithm());
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);

        // 表
        StandardShardingStrategyConfiguration tableShardingStrategyConfig =
                new StandardShardingStrategyConfiguration("u_id", new TableShardingAlgorithm(), null);
        tableRuleConfiguration.setTableShardingStrategyConfig(tableShardingStrategyConfig);


        list.add(tableRuleConfiguration);



        return list;
    }




    private ShardingRuleConfiguration getShardingRuleConfiguration() {

        // sharding总配置类
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        if ( false ) {

            // 设置全局默认库
            shardingRuleConfig.setDefaultDataSourceName("default");

            // 库
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("u_id", new DatabaseShardingAlgorithm()));

            // 表
            ComplexShardingStrategyConfiguration complexShardingStrategyConfiguration = new ComplexShardingStrategyConfiguration(
                    "u_id, year", new TableComplexKeysShardingAlgorithm());
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(complexShardingStrategyConfiguration);
        }

        return shardingRuleConfig;
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        return properties;
    }


    private Map<String, DataSource> getDatasourceMap() {


        Map<String, DataSource> dataSourceMap = new HashMap<>(4);

        // 配置第一个数据源db0
        // 约定 : 没有配置分库分表则使用default库
        DruidDataSource dataSourceDefault = new DruidDataSource();
        dataSourceDefault.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceDefault.setUrl("jdbc:mysql://127.0.0.1:3306/db0?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSourceDefault.setUsername("root");
        dataSourceDefault.setPassword("9527");
        dataSourceMap.put("db0", dataSourceDefault);

        // 配置第二个数据源db1
        DruidDataSource dataSourceDB1 = new DruidDataSource();
        dataSourceDB1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceDB1.setUrl("jdbc:mysql://127.0.0.1:3306/db1?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSourceDB1.setUsername("root");
        dataSourceDB1.setPassword("9527");
        dataSourceMap.put("db1", dataSourceDB1);

        // 配置第二个数据源db2
        DruidDataSource dataSourceDB2 = new DruidDataSource();
        dataSourceDB2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceDB2.setUrl("jdbc:mysql://127.0.0.1:3306/db2?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSourceDB2.setUsername("root");
        dataSourceDB2.setPassword("9527");
        dataSourceMap.put("db2", dataSourceDB2);

        // 配置第二个数据源db3
        DruidDataSource dataSourceDB3 = new DruidDataSource();
        dataSourceDB3.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceDB3.setUrl("jdbc:mysql://127.0.0.1:3306/db2?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSourceDB3.setUsername("root");
        dataSourceDB3.setPassword("9527");
        dataSourceMap.put("db3", dataSourceDB3);


        return dataSourceMap;
    }


    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }





}