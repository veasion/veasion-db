package cn.veasion.db.update;

import cn.veasion.db.base.Filter;

/**
 * Update
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class Update extends AbstractUpdate<Update> {

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

}
