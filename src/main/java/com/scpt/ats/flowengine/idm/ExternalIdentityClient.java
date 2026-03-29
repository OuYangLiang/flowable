package com.scpt.ats.flowengine.idm;

import java.util.List;
import java.util.Optional;

/**
 * 由业务系统实现：按用户 ID 提供用户信息与组关系（组 ID 需与 BPMN candidateGroups 一致）。
 */
public interface ExternalIdentityClient {

    Optional<ExternalUser> findUserById(String userId);

    List<String> listGroupIdsForUser(String userId);

    List<String> listUserIdsInGroup(String groupId);
}
