package com.scpt.ats.flowengine.idm;

import org.flowable.idm.api.Group;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.flowable.idm.engine.impl.GroupQueryImpl;
import org.flowable.idm.engine.impl.persistence.entity.GroupEntity;
import org.flowable.idm.engine.impl.persistence.entity.GroupEntityImpl;
import org.flowable.idm.engine.impl.persistence.entity.data.AbstractIdmDataManager;
import org.flowable.idm.engine.impl.persistence.entity.data.GroupDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 只读外部组：成员关系由 {@link ExternalIdentityClient} 提供。
 */
public class ExternalGroupDataManager extends AbstractIdmDataManager<GroupEntity> implements GroupDataManager {

    private final ExternalIdentityClient externalIdentityClient;

    public ExternalGroupDataManager(IdmEngineConfiguration idmEngineConfiguration,
                                    ExternalIdentityClient externalIdentityClient) {
        super(idmEngineConfiguration);
        this.externalIdentityClient = externalIdentityClient;
    }

    @Override
    public Class<? extends GroupEntity> getManagedEntityClass() {
        return GroupEntityImpl.class;
    }

    @Override
    public GroupEntity findById(String entityId) {
        if (entityId == null) {
            return null;
        }
        return toEntity(entityId);
    }

    @Override
    public GroupEntity create() {
        return new GroupEntityImpl();
    }

    @Override
    public void insert(GroupEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public GroupEntity update(GroupEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public void delete(GroupEntity entity) {
        throw new UnsupportedOperationException("External IDM is read-only");
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query) {
        if (query.getUserId() != null) {
            return new ArrayList<Group>(groupEntitiesForUser(query.getUserId()));
        }
        if (query.getUserIds() != null && !query.getUserIds().isEmpty()) {
            Set<String> ids = new LinkedHashSet<>();
            List<Group> all = new ArrayList<>();
            for (String uid : query.getUserIds()) {
                for (Group g : groupEntitiesForUser(uid)) {
                    if (ids.add(g.getId())) {
                        all.add(g);
                    }
                }
            }
            return new ArrayList<>(all);
        }
        if (query.getId() != null) {
            return Collections.singletonList(toEntity(query.getId()));
        }
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            List<Group> r = new ArrayList<>();
            for (String id : query.getIds()) {
                r.add(toEntity(id));
            }
            return r;
        }
        return Collections.emptyList();
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        return findGroupByQueryCriteria(query).size();
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        return new ArrayList<>(groupEntitiesForUser(userId));
    }

    private List<Group> groupEntitiesForUser(String userId) {
        return externalIdentityClient.listGroupIdsForUser(userId).stream()
                .map(this::toEntity)
                .map(g -> (Group) g)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> findGroupsByPrivilegeId(String privilegeId) {
        return Collections.emptyList();
    }

    @Override
    public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap) {
        return Collections.emptyList();
    }

    @Override
    public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
        return 0;
    }

    private GroupEntity toEntity(String groupId) {
        GroupEntityImpl g = new GroupEntityImpl();
        g.setId(groupId);
        g.setName(groupId);
        g.setType("security-role");
        return g;
    }
}
