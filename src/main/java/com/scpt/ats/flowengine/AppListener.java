package com.scpt.ats.flowengine;

import jakarta.annotation.Resource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ProcessEngine processEngine;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .name("flowengine-processes")
                    .addClasspathResource("holiday-request.bpmn20.xml")
                    .addClasspathResource("vas.bpmn20.xml")
                    .deploy();
        }
    }

}
