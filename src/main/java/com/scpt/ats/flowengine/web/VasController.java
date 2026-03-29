package com.scpt.ats.flowengine.web;

import com.scpt.ats.flowengine.common.RestResult;
import jakarta.annotation.Resource;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.Group;
import org.flowable.task.api.Task;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test/vas")
public class VasController {

    private static final String PROCESS_KEY = "vas";

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
                runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);
        return processInstance.getId();
    }

    /**
     * 待办：传 {@code userId} 时通过 Idm 解析所属组并查询候选任务；或传 {@code group} 直接按组查（兼容旧用法）。
     */
    @RequestMapping("/tasks")
    public RestResult<List<String>> tasks(@RequestParam(required = false) String userId,
                                          @RequestParam(required = false) String group) {
        TaskService taskService = processEngine.getTaskService();
        IdentityService identityService = processEngine.getIdentityService();
        List<Task> tasks;
        if (userId != null && !userId.isBlank()) {
            List<String> groups = identityService.createGroupQuery()
                    .groupMember(userId)
                    .list()
                    .stream()
                    .map(Group::getId)
                    .collect(Collectors.toList());
            if (groups.isEmpty()) {
                tasks = List.of();
            } else {
                tasks = taskService.createTaskQuery()
                        .processDefinitionKey(PROCESS_KEY)
                        .taskCandidateGroupIn(groups)
                        .list();
            }
        } else if (group != null && !group.isBlank()) {
            tasks = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_KEY)
                    .taskCandidateGroup(group)
                    .list();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide userId or group");
        }
        return RestResult.ok(tasks.stream()
                .map(task -> task.getId() + ":" + task.getName() + ":" + task.getTaskDefinitionKey())
                .collect(Collectors.toList()));
    }

    /**
     * 完成任务：必须传 {@code userId}，校验候选用户/组后再完成。
     */
    @RequestMapping("/complete")
    public String complete(@RequestParam String taskId,
                           @RequestParam String userId,
                           @RequestParam boolean approved) {
        TaskService taskService = processEngine.getTaskService();
        long allowed = taskService.createTaskQuery()
                .taskId(taskId)
                .taskCandidateOrAssigned(userId)
                .count();
        if (allowed == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to complete this task");
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.complete(taskId, variables);
        return "ok";
    }
}
