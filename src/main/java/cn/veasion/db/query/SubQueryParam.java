package cn.veasion.db.query;

/**
 * SubQueryParam
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class SubQueryParam {

    private AbstractQuery<?> query;

    public AbstractQuery<?> getQuery() {
        return query;
    }

    public void setQuery(AbstractQuery<?> query) {
        this.query = query;
    }
}
