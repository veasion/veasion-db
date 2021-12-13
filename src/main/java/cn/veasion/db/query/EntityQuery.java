package cn.veasion.db.query;

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

    @Override
    protected boolean isEmptySelects() {
        return getSelects().isEmpty() && getSelectExpression() == null && getSelectSubQueryList() == null && !isSelectAll();
    }

    @Override
    protected EntityQuery getSelf() {
        return this;
    }

}
