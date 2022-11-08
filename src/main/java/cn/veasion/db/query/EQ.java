package cn.veasion.db.query;

import cn.veasion.db.TableEntity;

/**
 * EQ
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class EQ extends EntityQuery {

    public EQ(Class<?> entityClass) {
        super(entityClass);
    }

    public EQ(Class<?> entityClass, String alias) {
        super(entityClass, alias);
    }

    public EQ(TableEntity tableEntity, String alias) {
        super(tableEntity, alias);
    }

    public EQ(TableEntity tableEntity) {
        super(tableEntity);
    }

}
