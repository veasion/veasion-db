package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.base.Operator;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;
import cn.veasion.db.utils.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AbstractJoinUpdate
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public abstract class AbstractJoinUpdate<T extends AbstractJoinUpdate<?>> extends AbstractUpdate<T> {

    protected String tableAs;
    protected Object entity;
    protected List<String> updateFields;
    protected List<JoinUpdateParam> joins;
    protected List<JoinUpdateParam> relations;
    protected boolean excludeUpdateFilterFields;

    public AbstractJoinUpdate(Object entity) {
        this(entity, null);
    }

    public AbstractJoinUpdate(Object entity, String alias) {
        this.entity = entity;
        setEntityClass(entity.getClass());
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
    }

    public AbstractJoinUpdate(Class<?> clazz) {
        this(clazz, null);
    }

    public AbstractJoinUpdate(Class<?> clazz, String alias) {
        this(TypeUtils.newInstance(clazz), alias);
    }

    public T updateFields(String... fields) {
        if (updateFields == null) updateFields = new ArrayList<>();
        updateFields.addAll(Arrays.asList(fields));
        return getSelf();
    }

    public T eq(String field) {
        return addFilter(Filter.eq(field, FieldUtils.getValue(entity, field)));
    }

    public T excludeUpdateFilterFields() {
        this.excludeUpdateFilterFields = true;
        return getSelf();
    }

    public JoinUpdateParam join(AbstractJoinUpdate<?> joinUpdate) {
        return join(joinUpdate, JoinTypeEnum.JOIN);
    }

    public JoinUpdateParam leftJoin(AbstractJoinUpdate<?> joinUpdate) {
        return join(joinUpdate, JoinTypeEnum.LEFT_JOIN);
    }

    public JoinUpdateParam rightJoin(AbstractJoinUpdate<?> joinUpdate) {
        return join(joinUpdate, JoinTypeEnum.RIGHT_JOIN);
    }

    public JoinUpdateParam fullJoin(AbstractJoinUpdate<?> joinUpdate) {
        return join(joinUpdate, JoinTypeEnum.FULL_JOIN);
    }

    private JoinUpdateParam join(AbstractJoinUpdate<?> joinUpdate, JoinType joinType) {
        if (joins == null) joins = new ArrayList<>();
        JoinUpdateParam joinQueryParam = new JoinUpdateParam(this, joinType, joinUpdate);
        joins.add(joinQueryParam);
        return joinQueryParam;
    }

    @Override
    protected String handleField(String field) {
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    public T updateExpression(String field, Expression expression) {
        if (expression == null) {
            return getSelf();
        }
        return super.updateExpression(field, expression.tableAs(tableAs));
    }

    @Override
    public T filterExpression(String field, Operator operator, Expression expression) {
        return super.filterExpression(field, operator, expression.tableAs(tableAs));
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check(Class<?> mainEntityClass) {
        if (joins != null) {
            relations = new ArrayList<>();
        }
        check(mainEntityClass, getSelf(), true);
    }

    private void check(Class<?> mainEntityClass, AbstractJoinUpdate<?> mainUpdate, boolean main) {
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
        super.check(mainEntityClass);
        if (joins != null) {
            for (JoinUpdateParam join : joins) {
                if (!main) {
                    mainUpdate.relations.add(join);
                }
                join.getJoinUpdate().check(mainEntityClass, mainUpdate, false);
            }
        }
    }

    private static boolean isEmptyUpdate(AbstractJoinUpdate<?> update) {
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
