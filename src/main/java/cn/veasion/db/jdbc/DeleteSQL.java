package cn.veasion.db.jdbc;

import cn.veasion.db.update.Delete;

import java.util.HashMap;

/**
 * DeleteSQL
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class DeleteSQL extends AbstractSQL<DeleteSQL> {

    private Delete delete;

    public DeleteSQL(Delete delete) {
        this.delete = delete;
    }

    public static DeleteSQL build(Delete delete) {
        return new DeleteSQL(delete).build();
    }

    @Override
    public DeleteSQL build() {
        this.reset();
        sql.append("DELETE FROM ").append(getTableName(delete.getEntityClass(), delete, delete)).append(" WHERE");
        appendFilter(new HashMap<String, Class<?>>() {{
            put(null, delete.getEntityClass());
        }}, delete.getFilters());
        return this;
    }

}
