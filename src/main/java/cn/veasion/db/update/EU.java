package cn.veasion.db.update;

/**
 * EU
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class EU extends EntityUpdate {

    public EU(Object entity) {
        super(entity);
    }

    public EU(Object entity, String alias) {
        super(entity, alias);
    }

    public EU(Class<?> clazz) {
        super(clazz);
    }

    public EU(Class<?> clazz, String alias) {
        super(clazz, alias);
    }

}
