package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.base.Operator;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractJoinQuery
 *
 * @author luozhuowei
 * @date 2021/12/11
 */
public abstract class AbstractJoinQuery<T extends AbstractJoinQuery<?>> extends AbstractQuery<T> {

    protected String tableAs;
    protected List<JoinQueryParam> joins;
    protected List<JoinQueryParam> relations;

    public JoinQueryParam join(AbstractJoinQuery<?> entityQuery) {
        return join(JoinTypeEnum.JOIN, entityQuery);
    }

    public JoinQueryParam leftJoin(AbstractJoinQuery<?> entityQuery) {
        return join(JoinTypeEnum.LEFT_JOIN, entityQuery);
    }

    public JoinQueryParam rightJoin(AbstractJoinQuery<?> entityQuery) {
        return join(JoinTypeEnum.RIGHT_JOIN, entityQuery);
    }

    public JoinQueryParam fullJoin(AbstractJoinQuery<?> entityQuery) {
        return join(JoinTypeEnum.FULL_JOIN, entityQuery);
    }

    public JoinQueryParam join(JoinType joinType, AbstractJoinQuery<?> entityQuery) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        JoinQueryParam joinQueryParam = new JoinQueryParam(this, joinType, entityQuery);
        joins.add(joinQueryParam);
        return joinQueryParam;
    }

    @Override
    public T selectExpression(Expression expression) {
        if (expression == null) {
            return getSelf();
        }
        return super.selectExpression(expression.tableAs(tableAs));
    }

    @Override
    public T filterExpression(String field, Operator operator, Expression expression) {
        return super.filterExpression(field, operator, expression.tableAs(tableAs));
    }

    @Override
    protected String handleField(String field) {
        if (aliasMap.containsKey(field)) {
            return field;
        }
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        if (aliasMap.containsKey(filter.getField()) || isAlias(filter.getField())) {
            return filter;
        }
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check(Class<?> mainEntityClass) {
        if (joins != null) {
            relations = new ArrayList<>();
        }
        check(mainEntityClass, this, true);
    }

    protected void check(Class<?> mainEntityClass, AbstractJoinQuery<?> mainQuery, boolean isMain) {
        if (isMain && mainQuery.isAllEmptySelects()) {
            selectAll();
        }
        super.check(mainEntityClass);
        if (joins != null) {
            for (JoinQueryParam join : joins) {
                if (!isMain) {
                    mainQuery.relations.add(join);
                }
                join.getJoinQuery().check(mainEntityClass, mainQuery, false);
            }
        }
    }

    protected boolean isAllEmptySelects() {
        boolean emptySelects = isEmptySelects();
        if (emptySelects && joins != null) {
            for (JoinQueryParam join : joins) {
                if (!join.getJoinQuery().isAllEmptySelects()) {
                    emptySelects = false;
                    break;
                }
            }
        }
        return emptySelects;
    }

    public String getTableAs() {
        return tableAs;
    }

    public List<JoinQueryParam> getJoins() {
        return joins;
    }

    public List<JoinQueryParam> getJoinAll() {
        if (joins == null || relations == null) {
            return joins;
        }
        List<JoinQueryParam> joinList = new ArrayList<>(joins.size() + relations.size());
        joinList.addAll(joins);
        joinList.addAll(relations);
        return joinList;
    }

    protected abstract boolean isEmptySelects();

}
