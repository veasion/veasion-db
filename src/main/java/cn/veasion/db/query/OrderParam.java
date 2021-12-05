package cn.veasion.db.query;

/**
 * OrderParam
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class OrderParam {

    private String field;
    private boolean asc = true;

    public OrderParam() {
    }

    public OrderParam(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }
}
