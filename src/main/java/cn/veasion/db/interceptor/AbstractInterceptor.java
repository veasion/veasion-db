package cn.veasion.db.interceptor;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Filter;
import cn.veasion.db.query.AbstractJoinQuery;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.query.UnionQueryParam;
import cn.veasion.db.query.With;
import cn.veasion.db.update.AbstractJoinUpdate;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.update.JoinUpdateParam;
import cn.veasion.db.utils.LeftRight;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * AbstractInterceptor
 *
 * @author luozhuowei
 * @date 2021/12/14
 */
public abstract class AbstractInterceptor implements EntityDaoInterceptor {

    private boolean handleQuery, handleUpdate, handleDelete, handleAbstractFilter, handleInsert;

    public AbstractInterceptor(boolean handleQuery, boolean handleUpdate, boolean handleDelete, boolean handleAbstractFilter, boolean handleInsert) {
        this.handleQuery = handleQuery;
        this.handleUpdate = handleUpdate;
        this.handleDelete = handleDelete;
        this.handleAbstractFilter = handleAbstractFilter;
        this.handleInsert = handleInsert;
    }

    @Override
    public <R> R intercept(EntityDaoInvocation<R> invocation) {
        Object[] args = invocation.getArgs();
        if (args != null && !skip()) {
            for (Object arg : args) {
                if (arg instanceof AbstractQuery && handleQuery) {
                    handleQuery((AbstractQuery<?>) arg);
                } else if (arg instanceof AbstractUpdate && handleUpdate) {
                    handleUpdate((AbstractUpdate<?>) arg);
                } else if (arg instanceof Delete && handleDelete) {
                    if (!containSkipClass(((Delete) arg).getEntityClass())) {
                        handleDelete((Delete) arg);
                    }
                } else if (arg instanceof AbstractFilter) {
                    if (handleAbstractFilter) {
                        handleAbstractFilter((AbstractFilter<?>) arg);
                    }
                } else if (arg instanceof BatchEntityInsert) {
                    if (!containSkipClass(((BatchEntityInsert) arg).getEntityClass())) {
                        handleBatchInsert((BatchEntityInsert) arg);
                    }
                } else if (arg instanceof EntityInsert && handleInsert) {
                    EntityInsert insert = (EntityInsert) arg;
                    if (!containSkipClass(insert.getEntityClass())) {
                        handleInsert(insert.getEntityClass(), Collections.singletonList(insert.getEntity()), Collections.singletonList(insert.getFieldValueMap()));
                    }
                }
            }
        }
        return invocation.proceed();
    }

    protected abstract void handleDelete(Delete delete);

    protected abstract void handleInsert(Class<?> entityClass, List<?> entityList, List<Map<String, Object>> fieldValueMapList);

    protected abstract void handleOnFilter(Object joinParam, Supplier<List<Filter>> onFilters, Consumer<Filter> onMethod, String tableAs);

    protected abstract void handleFilter(AbstractFilter<?> abstractFilter);

    protected boolean skip() {
        return false;
    }

    protected boolean containSkipClass(Class<?> clazz) {
        return false;
    }

    protected boolean containSkipClass(AbstractFilter<?> filter) {
        return filter != null && containSkipClass(filter.getEntityClass());
    }

    protected void handleQuery(AbstractQuery<?> query) {
        if (query instanceof SubQuery) {
            handleQuery(((SubQuery) query).getSubQuery());
        }
        if (query instanceof AbstractJoinQuery<?>) {
            With with = ((AbstractJoinQuery<?>) query).getWith();
            if (with != null && with.getWiths() != null) {
                for (LeftRight<AbstractJoinQuery<?>, String> withWith : with.getWiths()) {
                    handleQuery(withWith.getLeft());
                }
            }
        }
        handleSelectSubQuery(query.getSelectSubQueryList());
        handleAbstractFilter(query);
        if (query instanceof AbstractJoinQuery) {
            List<JoinQueryParam> joinList = ((AbstractJoinQuery<?>) query).getJoinAll();
            if (joinList != null) {
                for (JoinQueryParam joinQueryParam : joinList) {
                    AbstractJoinQuery<?> joinQuery = joinQueryParam.getJoinQuery();
                    if (joinQuery instanceof SubQuery) {
                        handleQuery(((SubQuery) joinQuery).getSubQuery());
                    } else {
                        if (!containSkipClass(joinQuery)) {
                            handleSelectSubQuery(joinQuery.getSelectSubQueryList());
                            handleOnFilter(joinQueryParam, joinQueryParam::getOnFilters, joinQueryParam::on, joinQuery.getTableAs());
                        }
                        handleFilterSubQuery(joinQuery.getFilters());
                    }
                }
            }
        }
        List<UnionQueryParam> unions = query.getUnions();
        if (unions != null) {
            unions.stream().map(UnionQueryParam::getUnion).forEach(this::handleQuery);
        }
    }

    protected void handleUpdate(AbstractUpdate<?> update) {
        handleAbstractFilter(update);
        if (update instanceof AbstractJoinUpdate<?>) {
            List<JoinUpdateParam> joinList = ((AbstractJoinUpdate<?>) update).getJoinAll();
            if (joinList == null || joinList.isEmpty()) return;
            for (JoinUpdateParam joinUpdateParam : joinList) {
                AbstractJoinUpdate<?> joinUpdate = joinUpdateParam.getJoinUpdate();
                if (!containSkipClass(joinUpdate)) {
                    handleOnFilter(joinUpdateParam, joinUpdateParam::getOnFilters, joinUpdateParam::on, joinUpdate.getTableAs());
                }
                handleFilterSubQuery(joinUpdate.getFilters());
            }
        }
    }

    protected void handleBatchInsert(BatchEntityInsert insert) {
        AbstractQuery<?> insertSelectQuery = insert.getInsertSelectQuery();
        if (insertSelectQuery != null) {
            if (handleQuery) {
                handleQuery(insertSelectQuery);
            }
        } else if (handleInsert) {
            handleInsert(insert.getEntityClass(), insert.getEntityList(), insert.getFieldValueMapList());
        }
    }

    protected void handleAbstractFilter(AbstractFilter<?> abstractFilter) {
        if (abstractFilter == null || abstractFilter.getTableEntity() != null || containSkipClass(abstractFilter)) {
            return;
        }
        if (!(abstractFilter instanceof SubQuery)) {
            handleFilter(abstractFilter);
        }
        handleFilterSubQuery(abstractFilter.getFilters());
    }

    protected void handleSelectSubQuery(List<SubQueryParam> list) {
        if (list == null || list.isEmpty()) return;
        for (SubQueryParam sub : list) {
            handleQuery(sub.getQuery());
        }
    }

    protected void handleFilterSubQuery(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return;
        for (Filter filter : filters) {
            if (filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
                handleQuery(((SubQueryParam) filter.getValue()).getQuery());
            }
        }
    }

}
