package com.scpt.ats.flowengine.common;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.flowable.engine.ProcessEngine;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
public class DataSourceConfig {

    private static final int DEFAULT_POOL_SIZE = 10;


    @Bean
    @Primary
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource rlt = new ComboPooledDataSource();
        rlt.setDriverClass("com.mysql.cj.jdbc.Driver");
        rlt.setUser("root");
        rlt.setPassword("pwd12345");
        rlt.setJdbcUrl("jdbc:mysql://localhost:3306/flowable");
        rlt.setInitialPoolSize(DEFAULT_POOL_SIZE);
        rlt.setMaxPoolSize(DEFAULT_POOL_SIZE);
        rlt.setMinPoolSize(DEFAULT_POOL_SIZE);
        rlt.setTestConnectionOnCheckin(true);
        rlt.setPreferredTestQuery("select 1");

        return rlt;
    }

    /*@Bean
    @Primary
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource rlt = new ComboPooledDataSource();
        rlt.setDriverClass("org.h2.Driver");
//        rlt.setJdbcUrl("jdbc:h2:mem:test;MODE=MySQL");
        rlt.setUser("sa");
        rlt.setPassword("");
        rlt.setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1");
        return rlt;
    }*/

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager rlt = new DataSourceTransactionManager();
        rlt.setDataSource(dataSource);

        return rlt;
    }

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, DataSourceTransactionManager transactionManager) {
        SpringProcessEngineConfiguration rlt = new SpringProcessEngineConfiguration();
        rlt.setDataSource(dataSource);
        rlt.setTransactionManager(transactionManager);
        rlt.setDatabaseSchemaUpdate("true");
        rlt.setAsyncExecutorActivate(false);

        return rlt;
    }

    @Bean
    public ProcessEngineFactoryBean processEngineFactoryBean(SpringProcessEngineConfiguration processEngineConfiguration) {
        ProcessEngineFactoryBean rlt = new ProcessEngineFactoryBean();
        rlt.setProcessEngineConfiguration(processEngineConfiguration);

        return rlt;
    }

    @Bean
    public ProcessEngine processEngine(ProcessEngineFactoryBean processEngineFactoryBean) throws Exception {
        return processEngineFactoryBean.getObject();
    }
}
