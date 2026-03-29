package com.scpt.ats.flowengine.common;

import com.scpt.ats.flowengine.idm.ExternalGroupDataManager;
import com.scpt.ats.flowengine.idm.ExternalIdentityClient;
import com.scpt.ats.flowengine.idm.ExternalUserDataManager;
import org.flowable.idm.spring.SpringIdmEngineConfiguration;
import org.flowable.idm.spring.configurator.SpringIdmEngineConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableIdmConfiguration {

    @Bean
    public SpringIdmEngineConfigurator springIdmEngineConfigurator(ExternalIdentityClient externalIdentityClient) {

        SpringIdmEngineConfiguration idmEngineConfiguration = new SpringIdmEngineConfiguration() {
            @Override
            public void initDataManagers() {
                setUserDataManager(new ExternalUserDataManager(this, externalIdentityClient));
                setGroupDataManager(new ExternalGroupDataManager(this, externalIdentityClient));
                super.initDataManagers();
            }
        };
        // IDM 引擎独立配置：仅作用于身份相关表（ACT_ID_*），与 SpringProcessEngineConfiguration 上的 databaseSchemaUpdate 各管一套
        // idmEngineConfiguration.setDatabaseSchemaUpdate("false");

        SpringIdmEngineConfigurator configurator = new SpringIdmEngineConfigurator();
        configurator.setIdmEngineConfiguration(idmEngineConfiguration);
        return configurator;
    }
}
