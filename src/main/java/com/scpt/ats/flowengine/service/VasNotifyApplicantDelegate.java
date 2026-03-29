package com.scpt.ats.flowengine.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * VAS 流程全部通过后通知申请人（可替换为真实邮件发送实现）。
 */
public class VasNotifyApplicantDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object applicant = execution.getVariable("applicant");
        Object serviceType = execution.getVariable("serviceType");
        Object startDate = execution.getVariable("startDate");
        Object endDate = execution.getVariable("endDate");
        System.out.println("[VAS] 审批已通过，发邮件通知申请人: " + applicant
                + "，服务类型=" + serviceType
                + "，开始=" + startDate
                + "，结束=" + endDate);
    }
}
