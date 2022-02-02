package cn.veasion.db.query;

/**
 * OrderParam
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class OrderParam {

    private String field;
    private Boolean desc;

    public OrderParam() {
    }

    public OrderParam(String field) {
        this.field = field;
    }

    public OrderParam(String field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isDesc() {
        return Boolean.TRUE.equals(desc);
    }

    public Boolean getDesc() {
        return desc;
    }

    public void setDesc(Boolean desc) {
        this.desc = desc;
    }
}
