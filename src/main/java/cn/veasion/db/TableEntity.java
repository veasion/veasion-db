package cn.veasion.db;

import java.io.Serializable;

/**
 * TableEntity
 *
 * @author luozhuowei
 * @date 2022/11/8
 */
public final class TableEntity implements Serializable {

    private String table;

    public TableEntity(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

}
