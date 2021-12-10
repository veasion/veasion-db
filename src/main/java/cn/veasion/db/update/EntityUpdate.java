package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;
import cn.veasion.db.utils.TypeUtils;

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
    private List<JoinUpdateParam> relations;
    private boolean excludeUpdateFilterFields;

    public EntityUpdate(Object entity) {
        this(entity, null);
    }

    public EntityUpdate(Object entity, String alias) {
        this.entity = entity;
        setEntityClass(entity.getClass());
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
    }

    public EntityUpdate(Class<?> clazz) {
        this(clazz, null);
    }

    public EntityUpdate(Class<?> clazz, String alias) {
        this(TypeUtils.newInstance(clazz), alias);
    }

    public EntityUpdate updateFields(String... fields) {
        if (updateFields == null) updateFields = new ArrayList<>();
        updateFields.addAll(Arrays.asList(fields));
        return this;
    }

    public EntityUpdate eq(String field) {
        return addFilter(Filter.eq(field, FieldUtils.getValue(entity, field)));
    }

    public EntityUpdate excludeUpdateFilterFields() {
        this.excludeUpdateFilterFields = true;
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
    protected String handleField(String field) {
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    public EntityUpdate updateExpression(String field, Expression expression) {
        if (expression == null) {
            return this;
        }
        return super.updateExpression(field, expression.tableAs(tableAs));
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check() {
        if (joins != null) {
            relations = new ArrayList<>();
        }
        check(this, true);
    }

    private void check(EntityUpdate mainUpdate, boolean main) {
        if (main && !checked && isEmptyUpdate(this)) {
            Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entity.getClass());
            if (updateFields == null) {
                updateFields = new ArrayList<>(fieldColumns.size());
            }
            updateFields.addAll(fieldColumns.keySet());
        }
        if (updateFields != null) {
            for (String updateField : updateFields) {
                if (excludeUpdateFilterFields && hasFilter(updateField)) {
                    continue;
                }
                update(updateField, FieldUtils.getValue(entity, updateField));
            }
        }
        super.check();
        if (joins != null) {
            for (JoinUpdateParam join : joins) {
                if (!main) {
                    mainUpdate.relations.add(join);
                }
                join.getJoinUpdate().check(mainUpdate, false);
            }
        }
    }

    private static boolean isEmptyUpdate(EntityUpdate update) {
        boolean emptyUpdate = update.getUpdates().isEmpty() && (update.updateFields == null || update.updateFields.isEmpty());
        if (emptyUpdate && update.joins != null) {
            for (JoinUpdateParam join : update.joins) {
                if (!isEmptyUpdate(join.getJoinUpdate())) {
                    emptyUpdate = false;
                    break;
                }
            }
        }
        return emptyUpdate;
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

    public List<JoinUpdateParam> getJoinAll() {
        if (joins == null || relations == null) {
            return joins;
        }
        List<JoinUpdateParam> joinList = new ArrayList<>(joins.size() + relations.size());
        joinList.addAll(joins);
        joinList.addAll(relations);
        return joinList;
    }

}
