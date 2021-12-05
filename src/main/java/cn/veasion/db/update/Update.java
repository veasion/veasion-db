package cn.veasion.db.update;

import cn.veasion.db.base.Filter;

/**
 * Update
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class Update extends AbstractUpdate<Update> {

    private Class<?> entityClass;

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Update setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
        return this;
    }

}
