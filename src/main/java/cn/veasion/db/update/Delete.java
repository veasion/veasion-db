package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Filter;

/**
 * Delete
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class Delete extends AbstractFilter<Delete> {

    private Class<?> entityClass;
    private AbstractUpdate<?> convertUpdate;

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Delete setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public AbstractUpdate<?> getConvertUpdate() {
        return convertUpdate;
    }

    public Delete convertUpdate(AbstractUpdate<?> convertUpdate) {
        this.convertUpdate = convertUpdate;
        return this;
    }

}
