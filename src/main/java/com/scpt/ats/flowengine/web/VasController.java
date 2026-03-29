package com.scpt.ats.flowengine.web;

import com.scpt.ats.flowengine.common.RestResult;
import jakarta.annotation.Resource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test/vas")
public class VasController {

    @Resource
    private ProcessEngine processEngine;

    /**
     * 启动 VAS 流程。变量：applicant、serviceType、startDate、endDate。
     */
    @RequestMapping("/apply")
    public String apply(@RequestParam String applicant,
                        @RequestParam String serviceType,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicant", applicant);
        variables.put("serviceType", serviceType);
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("vas", variables);
        return processInstance.getId();
    }

    /**
     * 按候选组查询待办（pcoc / rtd / finance / aro）。
     */
    @RequestMapping("/tasks")
    public RestResult<List<String>> tasks(@RequestParam String group) {
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(group).list();
        return RestResult.ok(tasks.stream()
                .map(task -> task.getId() + ":" + task.getName() + ":" + task.getTaskDefinitionKey())
                .collect(Collectors.toList()));
    }

    /**
     * 完成任务：传入 taskId 与是否同意（approved）。
     */
    @RequestMapping("/complete")
    public String complete(@RequestParam String taskId, @RequestParam boolean approved) {
        TaskService taskService = processEngine.getTaskService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.complete(taskId, variables);
        return "ok";
    }
}
