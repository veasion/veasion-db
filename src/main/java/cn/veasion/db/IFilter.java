package cn.veasion.db;

import cn.veasion.db.base.Filter;

/**
 * IFilter
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public interface IFilter<T> {

    T addFilter(Filter filter);

}
