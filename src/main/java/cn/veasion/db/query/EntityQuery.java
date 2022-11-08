package cn.veasion.db.query;

import cn.veasion.db.TableEntity;
import cn.veasion.db.utils.LeftRight;

/**
 * EntityQuery
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class EntityQuery extends AbstractJoinQuery<EntityQuery> {

    private With with;

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

    public With getWith() {
        return with;
    }

    protected void setWith(With with) {
        this.with = with;
    }

    @Override
    public void check(Class<?> mainEntityClass) {
        super.check(mainEntityClass);
        if (with != null && with.getWiths() != null) {
            with.getWiths().stream().map(LeftRight::getLeft).forEach(q -> q.check(mainEntityClass));
        }
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
