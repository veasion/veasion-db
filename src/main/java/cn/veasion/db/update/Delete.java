package cn.veasion.db.update;

import cn.veasion.db.base.AbstractFilter;
import cn.veasion.db.base.Filter;

/**
 * Delete
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class Delete extends AbstractFilter<Delete> {

    private Class<?> entityClass;

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
}
