package com.scpt.ats.flowengine.web;

import com.scpt.ats.flowengine.common.RestResult;
import jakarta.annotation.Resource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TestController {

    @Resource
    private ProcessEngine processEngine;

    @RequestMapping("/test/init")
    public String init() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        return processDefinition.getName();
    }

    @RequestMapping("/test/apply")
    public String apply(@RequestParam String who, @RequestParam int numberOfDays, @RequestParam String reason) {
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", who);
        variables.put("nrOfHolidays", numberOfDays);
        variables.put("description", reason);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);

        return processInstance.getId();
    }

    @RequestMapping("/test/tasks")
    public RestResult<List<String>> tasks() {
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        /*StringBuilder sb = new StringBuilder();
        sb.append("You have ").append(tasks.size()).append(" tasks.\n");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
            sb.append(i + 1).append(") ").append(tasks.get(i).getName()).append("\n");
        }

        return sb.toString();*/

        return RestResult.ok(tasks.stream().map(task -> task.getId() + ":" + task.getName() + ":" + task.getTaskDefinitionKey()).collect(Collectors.toList()));
    }

    @RequestMapping("/test/approve")
    public String approve(@RequestParam int taskIndex, @RequestParam boolean approve) {
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();

        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approve);
        taskService.complete(task.getId(), variables);

        return "ok";
    }

    @RequestMapping("/test/hello")
    public String hello() {
        return "hello";
    }
}
