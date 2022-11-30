package cn.veasion.db.query;

import java.util.List;

/**
 * PostgrePage
 *
 * @author luozhuowei
 * @date 2022/11/29
 */
public class PostgrePage extends PageParam {

    public PostgrePage() {
    }

    public PostgrePage(int page, int size) {
        super(page, size);
    }

    @Override
    public void handleSqlValue(StringBuilder sql, List<Object> values) {
        sql.append(" LIMIT ? OFFSET ? ");
        values.add(size);
        values.add((page - 1) * size);
    }

}
