package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.update.JoinUpdateParam;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.LeftRight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UpdateSQL
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class UpdateSQL extends AbstractSQL<UpdateSQL> {

    protected AbstractUpdate<?> update;

    private List<JoinUpdateParam> joins;
    private Map<String, Object> tableEntityMap;

    public UpdateSQL(AbstractUpdate<?> update) {
        this.update = update;
    }

    public static UpdateSQL build(AbstractUpdate<?> update) {
        return new UpdateSQL(update).build();
    }

    @Override
    public UpdateSQL build() {
        this.reset();
        buildUpdate();
        return this;
    }

    public void buildUpdate() {
        tableEntityMap = new HashMap<>();
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        sql.append("UPDATE ");
        sql.append(getTableName(update.getEntityClass(), update, update));
        if (update instanceof EntityUpdate) {
            joins = ((EntityUpdate) update).getJoinAll();
            String tableAs = ((EntityUpdate) update).getTableAs();
            if (tableAs != null) {
                sql.append(" ").append(tableAs);
            }
            entityClassMap.put(tableAs, update.getEntityClass());
            tableEntityMap.put(tableAs, ((EntityUpdate) update).getEntity());
            if (joins != null) {
                for (JoinUpdateParam join : joins) {
                    EntityUpdate joinUpdate = join.getJoinUpdate();
                    entityClassMap.put(joinUpdate.getTableAs(), joinUpdate.getEntityClass());
                    tableEntityMap.put(joinUpdate.getTableAs(), joinUpdate.getEntity());
                }
            }
        } else {
            entityClassMap.put(null, update.getEntityClass());
        }
        // join on
        appendJoinOn();
        // set
        sql.append(" SET");
        // update
        appendUpdateAll(entityClassMap);
        sql.append(" WHERE");
        // filter
        appendFilters(entityClassMap);
        trimEndSql("WHERE");
    }

    private void appendJoinOn() {
        if (joins == null || joins.isEmpty()) return;
        for (JoinUpdateParam join : joins) {
            EntityUpdate mainUpdate = join.getMainUpdate();
            EntityUpdate joinUpdate = join.getJoinUpdate();
            sql.append(" ").append(join.getJoinType().getJoin());
            sql.append(" ").append(getTableName(joinUpdate.getEntityClass(), joinUpdate, join));
            if (joinUpdate.getTableAs() != null) {
                sql.append(" ").append(joinUpdate.getTableAs());
            }
            List<Filter> onFilters = join.getOnFilters();
            if (onFilters != null && onFilters.size() > 0) {
                sql.append(" ON");
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainUpdate.getTableAs(), mainUpdate.getEntityClass());
                    put(joinUpdate.getTableAs(), joinUpdate.getEntityClass());
                }}, onFilters);
            }
        }
    }

    private void appendUpdateAll(Map<String, Class<?>> entityClassMap) {
        appendUpdates(entityClassMap, update.getUpdates());
        if (joins == null || joins.isEmpty()) return;
        for (JoinUpdateParam join : joins) {
            EntityUpdate mainUpdate = join.getMainUpdate();
            EntityUpdate joinUpdate = join.getJoinUpdate();
            if (joinUpdate.getUpdates() != null) {
                sql.append(",");
                appendUpdates(new HashMap<String, Class<?>>() {{
                    put(mainUpdate.getTableAs(), mainUpdate.getEntityClass());
                    put(joinUpdate.getTableAs(), joinUpdate.getEntityClass());
                }}, joinUpdate.getUpdates());
            }
        }
    }

    private void appendUpdates(Map<String, Class<?>> entityClassMap, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sql.append(" ").append(handleFieldToColumn(key, entityClassMap)).append(" = ");
            if (value instanceof Expression) {
                Expression expression = (Expression) value;
                appendExpressionValue(entityClassMap, expression);
            } else {
                sql.append("?");
                values.add(value);
            }
            sql.append(",");
        }
        trimEndSql(",");
    }

    private void appendFilters(Map<String, Class<?>> entityClassMap) {
        appendFilter(entityClassMap, update.getFilters());
        if (joins == null || joins.isEmpty()) return;
        for (JoinUpdateParam join : joins) {
            EntityUpdate mainUpdate = join.getMainUpdate();
            EntityUpdate joinUpdate = join.getJoinUpdate();
            if (joinUpdate.hasFilters()) {
                if (!endsWith(" WHERE")) {
                    sql.append(" AND");
                }
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainUpdate.getTableAs(), mainUpdate.getEntityClass());
                    put(joinUpdate.getTableAs(), joinUpdate.getEntityClass());
                }}, joinUpdate.getFilters());
            }
        }
    }

    @Override
    protected LeftRight<Boolean, Object> expressionValue(String tableAs, String field) {
        if (tableEntityMap == null) return null;
        Object entity = tableEntityMap.get(tableAs);
        if (entity == null && tableEntityMap.size() == 1) {
            entity = tableEntityMap.values().iterator().next();
        }
        if (entity == null) {
            if (field.startsWith("value")) {
                return LeftRight.build(Boolean.FALSE, null);
            } else {
                throw new DbException("获取字段失败: " + field + "，对象为空");
            }
        }
        Object value = FieldUtils.getValue(entity, field, false);
        if (value != null) {
            return LeftRight.build(Boolean.TRUE, value);
        } else if (field.startsWith("value")) {
            return LeftRight.build(Boolean.FALSE, null);
        } else {
            throw new DbException("字段不存在: " + field + " => " + entity.getClass().getName());
        }
    }

}
