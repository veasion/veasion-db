package cn.veasion.db;

import cn.veasion.db.base.Filter;
import cn.veasion.db.utils.TypeUtils;

import java.io.Serializable;

/**
 * IFilter
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public interface IFilter<T> extends Serializable {

    T addFilter(Filter filter);

    @SuppressWarnings("unchecked")
    default T copy() {
        try {
            return (T) TypeUtils.serializableCopy(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
