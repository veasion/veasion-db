package cn.veasion.db.update;

/**
 * U
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class U extends Update {

    public U() {
    }

    public U(String field, Object value) {
        super(field, value);
    }

    public U(String field1, Object value1, String field2, Object value2) {
        super(field1, value1, field2, value2);
    }

}
