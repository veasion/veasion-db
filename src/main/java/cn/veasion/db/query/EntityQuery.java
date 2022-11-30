package cn.veasion.db.query;

import cn.veasion.db.TableEntity;

/**
 * EntityQuery
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class EntityQuery extends AbstractJoinQuery<EntityQuery> {

    public EntityQuery(Class<?> entityClass) {
        this(entityClass, null);
    }

    public EntityQuery(Class<?> entityClass, String alias) {
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
        setEntityClass(entityClass);
    }

    public EntityQuery(TableEntity tableEntity, String alias) {
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
        setTableEntity(tableEntity);
    }

    public EntityQuery(TableEntity tableEntity) {
        this(tableEntity, null);
    }

    @Override
    protected EntityQuery getSelf() {
        return this;
    }

}
