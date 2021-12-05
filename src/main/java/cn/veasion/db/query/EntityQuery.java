package cn.veasion.db.query;

import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityQuery
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class EntityQuery extends AbstractQuery<EntityQuery> {

    private String tableAs;
    private Class<?> entityClass;
    private Class<?> resultClass;

    private List<JoinQueryParam> joins;

    public EntityQuery(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.resultClass = entityClass;
    }

    public EntityQuery(Class<?> entityClass, String alias) {
        this(entityClass);
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
    }

    public JoinQueryParam join(EntityQuery entityQuery) {
        return join(entityQuery, JoinTypeEnum.JOIN);
    }

    public JoinQueryParam leftJoin(EntityQuery entityQuery) {
        return join(entityQuery, JoinTypeEnum.LEFT_JOIN);
    }

    public JoinQueryParam rightJoin(EntityQuery entityQuery) {
        return join(entityQuery, JoinTypeEnum.RIGHT_JOIN);
    }

    public JoinQueryParam fullJoin(EntityQuery entityQuery) {
        return join(entityQuery, JoinTypeEnum.FULL_JOIN);
    }

    private JoinQueryParam join(EntityQuery entityQuery, JoinTypeEnum joinType) {
        if (joins == null) joins = new ArrayList<>();
        JoinQueryParam joinQueryParam = new JoinQueryParam(this, joinType, entityQuery);
        joins.add(joinQueryParam);
        return joinQueryParam;
    }

    public EntityQuery withResultClass(Class<?> resultClass) {
        this.resultClass = resultClass;
        return this;
    }

    @Override
    protected String handleSelectField(String field) {
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    public String getTableAs() {
        return tableAs;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public List<JoinQueryParam> getJoins() {
        return joins;
    }

}
