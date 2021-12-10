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
        sql.append("SELECT t.* FROM (SELECT  t.*, ROWNUM AS row FROM (");
        sql.append(querySql);
        sql.append(") t WHERE ROWNUM <= ?) t WHERE t.row > ?");
        values.add(page * size);
        values.add(page * size - size);
    }

}
