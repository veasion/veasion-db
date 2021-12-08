package cn.veasion.db.query;

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

}
