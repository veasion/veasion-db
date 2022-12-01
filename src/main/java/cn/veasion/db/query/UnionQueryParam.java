package cn.veasion.db.query;

import java.io.Serializable;

/**
 * UnionQueryParam
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class UnionQueryParam implements Serializable {

    private boolean unionAll;
    private AbstractQuery<?> union;

    public UnionQueryParam(AbstractQuery<?> union, boolean unionAll) {
        this.unionAll = unionAll;
        this.union = union;
    }

    public boolean isUnionAll() {
        return unionAll;
    }

    public AbstractQuery<?> getUnion() {
        return union;
    }

}
