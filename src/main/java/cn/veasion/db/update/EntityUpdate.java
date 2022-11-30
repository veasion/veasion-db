package cn.veasion.db.update;

/**
 * EntityUpdate
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class EntityUpdate extends AbstractJoinUpdate<EntityUpdate> {

    public EntityUpdate(Object entity) {
        super(entity);
    }

    public EntityUpdate(Object entity, String alias) {
        super(entity, alias);
    }

    public EntityUpdate(Class<?> clazz) {
        super(clazz);
    }

    public EntityUpdate(Class<?> clazz, String alias) {
        super(clazz, alias);
    }

    @Override
    protected EntityUpdate getSelf() {
        return this;
    }

}
