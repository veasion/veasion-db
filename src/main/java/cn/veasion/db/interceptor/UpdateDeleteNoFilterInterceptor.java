package cn.veasion.db.interceptor;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.jdbc.DeleteSQL;
import cn.veasion.db.update.AbstractJoinUpdate;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.JoinUpdateParam;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 拦截器：更新、删除 操作必须携带过滤条件
 *
 * @author luozhuowei
 * @date 2022/12/1
 */
public class UpdateDeleteNoFilterInterceptor extends AbstractInterceptor {

    public UpdateDeleteNoFilterInterceptor() {
        super(false, true, true, false, false);
    }

    protected boolean hasFilter(AbstractFilter<?> abstractFilter) {
        return !containSkipClass(abstractFilter) && abstractFilter.getFilters() != null && abstractFilter.getFilters().size() > 0;
    }

    @Override
    protected void handleUpdate(AbstractUpdate<?> update) {
        boolean noFilter = true;
        if (hasFilter(update)) {
            noFilter = false;
        } else if (update instanceof AbstractJoinUpdate<?>) {
            List<JoinUpdateParam> joinList = ((AbstractJoinUpdate<?>) update).getJoinAll();
            if (joinList != null && joinList.size() > 0) {
                for (JoinUpdateParam joinUpdateParam : joinList) {
                    AbstractJoinUpdate<?> joinUpdate = joinUpdateParam.getJoinUpdate();
                    if (joinUpdate == null) {
                        continue;
                    }
                    if (hasFilter(joinUpdate)) {
                        noFilter = false;
                        break;
                    }
                }
            }
        }
        if (noFilter) {
            String sql = update.sqlValue().getSQL();
            throw new DbException("不允许全量更新操作，必须携带过滤条件 => " + sql);
        }
    }

    @Override
    protected void handleDelete(Delete delete) {
        if (!hasFilter(delete)) {
            String sql = DeleteSQL.build(delete).getSQL();
            throw new DbException("不允许全量删除操作，必须携带过滤条件 => " + sql);
        }
    }

    @Override
    protected void handleInsert(Class<?> entityClass, List<?> entityList, List<Map<String, Object>> fieldValueMapList) {

    }

    @Override
    protected void handleOnFilter(Object joinParam, Supplier<List<Filter>> onFilters, Consumer<Filter> onMethod, String tableAs) {

    }

    @Override
    protected void handleFilter(AbstractFilter<?> abstractFilter) {

    }

    @Override
    public int sortIndex() {
        return 1;
    }

}
