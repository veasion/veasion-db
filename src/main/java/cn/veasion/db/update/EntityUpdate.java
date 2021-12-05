package cn.veasion.db.update;

import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.utils.FieldUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * EntityUpdate
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class EntityUpdate extends AbstractUpdate<EntityUpdate> {

    private String tableAs;
    private Object entity;
    private List<String> updateFields;
    private List<JoinUpdateParam> joins;

    public EntityUpdate(Object entity) {
        this.entity = entity;
    }

    public EntityUpdate(Object entity, String alias) {
        this(entity);
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
    }

    public EntityUpdate updateFields(String... fields) {
        if (updateFields == null) updateFields = new ArrayList<>();
        updateFields.addAll(Arrays.asList(fields));
        return this;
    }

    public JoinUpdateParam join(EntityUpdate entityUpdate) {
        return join(entityUpdate, JoinTypeEnum.JOIN);
    }

    public JoinUpdateParam leftJoin(EntityUpdate entityUpdate) {
        return join(entityUpdate, JoinTypeEnum.LEFT_JOIN);
    }

    public JoinUpdateParam rightJoin(EntityUpdate entityUpdate) {
        return join(entityUpdate, JoinTypeEnum.RIGHT_JOIN);
    }

    public JoinUpdateParam fullJoin(EntityUpdate entityUpdate) {
        return join(entityUpdate, JoinTypeEnum.FULL_JOIN);
    }

    private JoinUpdateParam join(EntityUpdate entityUpdate, JoinTypeEnum joinType) {
        if (joins == null) joins = new ArrayList<>();
        JoinUpdateParam joinQueryParam = new JoinUpdateParam(this, joinType, entityUpdate);
        joins.add(joinQueryParam);
        return joinQueryParam;
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check() {
        if (updateFields == null || updateFields.isEmpty()) {
            Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entity.getClass());
            updateFields.addAll(fieldColumns.keySet());
        }
        for (String updateField : updateFields) {
            update(updateField, FieldUtils.getValue(entity, updateField));
        }
        super.check();
    }

    public String getTableAs() {
        return tableAs;
    }

    public Object getEntity() {
        return entity;
    }

    public List<JoinUpdateParam> getJoins() {
        return joins;
    }
}
