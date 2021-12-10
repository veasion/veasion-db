package cn.veasion.db.query;

import java.util.List;

/**
 * OraclePage
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class OraclePage extends PageParam {

    public OraclePage() {
    }

    public OraclePage(int page, int size) {
        super(page, size);
    }

    @Override
    public void handleSqlValue(StringBuilder sql, List<Object> values) {
        String querySql = sql.toString();
        sql.setLength(0);
        sql.append("select t.* from (select  t.*, ROWNUM as row from (");
        sql.append(querySql);
        sql.append(") t where ROWNUM <= ?) t where t.row > ?");
        values.add(page * size);
        values.add(page * size - size);
    }

}
