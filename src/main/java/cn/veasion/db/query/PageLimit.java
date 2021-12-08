package cn.veasion.db.query;

import java.util.List;

/**
 * PageLimit
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class PageLimit extends PageParam {

    public PageLimit(int page, int size) {
        super(page, size);
    }

    @Override
    public void handleSqlValue(StringBuilder sql, List<Object> values) {
        sql.append(" limit ?, ? ");
        values.add((getPage() - 1) * getSize());
        values.add(getSize());
    }

}
