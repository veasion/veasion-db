package cn.veasion.db.query;

import cn.veasion.db.base.Filter;

/**
 * Query
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class Query extends AbstractQuery<Query> {

    private Class<?> entityClass;

    @Override
    protected String handleSelectField(String field) {
        return field;
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Query setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
        return this;
    }
}
