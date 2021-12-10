package cn.veasion.db.jdbc;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.update.JoinUpdateParam;

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
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        sql.append("UPDATE ");
        sql.append(getTableName(update.getEntityClass()));
        if (update instanceof EntityUpdate) {
            joins = ((EntityUpdate) update).getJoinAll();
            String tableAs = ((EntityUpdate) update).getTableAs();
            if (tableAs != null) {
                sql.append(" ").append(tableAs);
            }
            entityClassMap.put(tableAs, update.getEntityClass());
            if (joins != null) {
                joins.forEach(q -> entityClassMap.put(q.getJoinUpdate().getTableAs(), q.getJoinUpdate().getEntityClass()));
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
            sql.append(" ").append(getTableName(joinUpdate.getEntityClass()));
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
                sql.append(" AND");
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainUpdate.getTableAs(), mainUpdate.getEntityClass());
                    put(joinUpdate.getTableAs(), joinUpdate.getEntityClass());
                }}, joinUpdate.getFilters());
            }
        }
    }

}
