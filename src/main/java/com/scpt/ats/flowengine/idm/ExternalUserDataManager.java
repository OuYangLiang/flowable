package com.scpt.ats.flowengine.idm;

import org.flowable.idm.api.User;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.flowable.idm.engine.impl.UserQueryImpl;
import org.flowable.idm.engine.impl.persistence.entity.UserEntity;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
import org.flowable.idm.engine.impl.persistence.entity.data.AbstractIdmDataManager;
import org.flowable.idm.engine.impl.persistence.entity.data.UserDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 只读外部用户：查询走 {@link ExternalIdentityClient}，不落 Flowable IDM 表。
 */
public class ExternalUserDataManager extends AbstractIdmDataManager<UserEntity> implements UserDataManager {

    private final ExternalIdentityClient externalIdentityClient;

    public ExternalUserDataManager(IdmEngineConfiguration idmEngineConfiguration,
                                   ExternalIdentityClient externalIdentityClient) {
        super(idmEngineConfiguration);
        this.externalIdentityClient = externalIdentityClient;
    }

    @Override
    public Class<? extends UserEntity> getManagedEntityClass() {
        return UserEntityImpl.class;
    }

    @Override
    public UserEntity findById(String entityId) {
        if (entityId == null) {
            return null;
        }
        return externalIdentityClient.findUserById(entityId)
                .map(this::toEntity)
                .orElse(null);
    }

    @Override
    public UserEntity create() {
        return new UserEntityImpl();
    }

    @Override
    public void insert(UserEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public UserEntity update(UserEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public void delete(UserEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query) {
        if (query.getId() != null) {
            return externalIdentityClient.findUserById(query.getId())
                    .map(u -> (User) toEntity(u))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        }
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            return query.getIds().stream()
                    .map(externalIdentityClient::findUserById)
                    .filter(Optional::isPresent)
                    .map(opt -> toEntity(opt.get()))
                    .map(u -> (User) u)
                    .collect(Collectors.toList());
        }
        if (query.getGroupId() != null) {
            return toUserList(externalIdentityClient.listUserIdsInGroup(query.getGroupId()));
        }
        if (query.getGroupIds() != null && !query.getGroupIds().isEmpty()) {
            List<User> all = new ArrayList<>();
            for (String gid : query.getGroupIds()) {
                all.addAll(toUserList(externalIdentityClient.listUserIdsInGroup(gid)));
            }
            return all;
        }
        return Collections.emptyList();
    }

    private List<User> toUserList(List<String> userIds) {
        List<User> r = new ArrayList<>();
        for (String uid : userIds) {
            externalIdentityClient.findUserById(uid).ifPresent(u -> r.add(toEntity(u)));
        }
        return r;
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        return findUserByQueryCriteria(query).size();
    }

    @Override
    public List<User> findUsersByPrivilegeId(String privilegeId) {
        return Collections.emptyList();
    }

    @Override
    public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap) {
        return Collections.emptyList();
    }

    @Override
    public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
        return 0;
    }

    private UserEntity toEntity(ExternalUser u) {
        UserEntityImpl e = new UserEntityImpl();
        e.setId(u.getId());
        e.setDisplayName(u.getDisplayName());
        e.setEmail(u.getEmail() != null ? u.getEmail() : "");
        return e;
    }
}
