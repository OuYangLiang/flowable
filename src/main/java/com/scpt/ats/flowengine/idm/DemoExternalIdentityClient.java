package com.scpt.ats.flowengine.idm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 演示用外部身份源：用内存映射代替 HTTP 调用，上线请替换为实现 {@link ExternalIdentityClient} 的真实 Bean。
 */
@Component
public class DemoExternalIdentityClient implements ExternalIdentityClient {

    private final Map<String, ExternalUser> users = new HashMap<>();
    private final Map<String, List<String>> userToGroups = new HashMap<>();

    public DemoExternalIdentityClient() {
        put("u_pcoc", "PCOC 用户", "pcoc@demo", List.of("pcoc"));
        put("u_rtd", "RTD 用户", "rtd@demo", List.of("rtd"));
        put("u_finance", "Finance 用户", "fin@demo", List.of("finance"));
        put("u_aro", "ARO 用户", "aro@demo", List.of("aro"));
        put("u_approver_all", "多角色审批", "all@demo", List.of("pcoc", "rtd", "finance", "aro"));
    }

    private void put(String id, String name, String email, List<String> groups) {
        users.put(id, new ExternalUser(id, name, email));
        userToGroups.put(id, List.copyOf(groups));
    }

    @Override
    public Optional<ExternalUser> findUserById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<String> listGroupIdsForUser(String userId) {
        return userToGroups.getOrDefault(userId, List.of());
    }

    @Override
    public List<String> listUserIdsInGroup(String groupId) {
        List<String> r = new ArrayList<>();
        userToGroups.forEach((uid, groups) -> {
            if (groups.contains(groupId)) {
                r.add(uid);
            }
        });
        return r;
    }
}
