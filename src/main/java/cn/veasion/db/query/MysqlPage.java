package cn.veasion.db.query;

import java.util.List;

/**
 * MysqlPage
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class MysqlPage extends PageParam {

    public MysqlPage() {
    }

    public MysqlPage(int page, int size) {
        super(page, size);
    }

    @Override
    public void handleSqlValue(StringBuilder sql, List<Object> values) {
        sql.append(" limit ?, ? ");
        values.add((page - 1) * size);
        values.add(size);
    }

}
