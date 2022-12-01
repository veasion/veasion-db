package cn.veasion.db.base;

import java.io.Serializable;

/**
 * IBaseId
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public interface IBaseId<ID> extends Serializable {

    ID getId();

    void setId(ID id);

}
