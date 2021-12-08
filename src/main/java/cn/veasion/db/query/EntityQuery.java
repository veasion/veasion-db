package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
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
    private List<JoinQueryParam> joins;

    public EntityQuery(Class<?> entityClass) {
        this(entityClass, null);
    }

    public EntityQuery(Class<?> entityClass, String alias) {
        setEntityClass(entityClass);
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

    @Override
    public EntityQuery selectExpression(Expression expression) {
        if (expression == null) {
            return this;
        }
        return super.selectExpression(expression.tableAs(tableAs));
    }

    @Override
    protected String handleField(String field) {
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check() {
        check(true);
    }

    private void check(boolean main) {
        if (main && isEmptySelects(this)) {
            selectAll();
        }
        super.check();
        if (joins != null) {
            for (JoinQueryParam join : joins) {
                join.getJoinEntityQuery().check(false);
            }
        }
    }

    private static boolean isEmptySelects(EntityQuery query) {
        boolean emptySelect = query.getSelects().isEmpty() && query.getSelectExpression() == null && !query.isSelectAll();
        if (emptySelect && query.joins != null) {
            for (JoinQueryParam join : query.joins) {
                if (!isEmptySelects(join.getJoinEntityQuery())) {
                    emptySelect = false;
                    break;
                }
            }
        }
        return emptySelect;
    }

    public String getTableAs() {
        return tableAs;
    }

    public List<JoinQueryParam> getJoins() {
        return joins;
    }

}
