package cn.veasion.db.base;

/**
 * IBaseId
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public interface IBaseId<ID> {

    ID getId();

    void setId(ID id);

}
