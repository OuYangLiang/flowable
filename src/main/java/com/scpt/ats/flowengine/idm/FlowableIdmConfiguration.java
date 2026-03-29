package com.scpt.ats.flowengine.idm;

import org.flowable.idm.spring.SpringIdmEngineConfiguration;
import org.flowable.idm.spring.configurator.SpringIdmEngineConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class FlowableIdmConfiguration {

    @Bean
    public SpringIdmEngineConfigurator springIdmEngineConfigurator(
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
            ExternalIdentityClient externalIdentityClient) {

        SpringIdmEngineConfiguration idmEngineConfiguration = new SpringIdmEngineConfiguration() {
            @Override
            public void initDataManagers() {
                setUserDataManager(new ExternalUserDataManager(this, externalIdentityClient));
                setGroupDataManager(new ExternalGroupDataManager(this, externalIdentityClient));
                super.initDataManagers();
            }
        };
        idmEngineConfiguration.setDataSource(dataSource);
        idmEngineConfiguration.setTransactionManager(transactionManager);
        idmEngineConfiguration.setDatabaseSchemaUpdate("true");

        SpringIdmEngineConfigurator configurator = new SpringIdmEngineConfigurator();
        configurator.setIdmEngineConfiguration(idmEngineConfiguration);
        return configurator;
    }
}
