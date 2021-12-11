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

    private AbstractUpdate<?> convertUpdate;

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    public AbstractUpdate<?> getConvertUpdate() {
        return convertUpdate;
    }

    public Delete convertUpdate(AbstractUpdate<?> convertUpdate) {
        this.convertUpdate = convertUpdate;
        return this;
    }

    @Override
    protected Delete getSelf() {
        return this;
    }

}
