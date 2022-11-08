package cn.veasion.db.query;

import cn.veasion.db.utils.LeftRight;

import java.util.ArrayList;
import java.util.List;

/**
 * With
 *
 * @author luozhuowei
 * @date 2022/11/7
 */
public class With {

    private boolean recursive;
    private boolean asAfter;
    private List<LeftRight<EntityQuery, String>> withs = new ArrayList<>();

    private With() {
    }

    public static With build() {
        return new With();
    }

    /**
     * with 递归
     */
    public static With buildRecursive() {
        With with = new With();
        with.recursive = true;
        return with;
    }

    /**
     * mysql数据库with查询的as是在()前，如 with xxx as (...) <br>
     * clickhouse数据库with查询的as是在()后，如 with (...) as xxx <br>
     * 当前With默认为mysql格式，这里可以通过当前buildAsAfter来构建一个clickhouse这种as在后的with对象
     */
    public static With buildAsAfter() {
        With with = new With();
        with.asAfter = true;
        return with;
    }

    public With with(EntityQuery entityQuery, String as) {
        withs.add(new LeftRight<>(entityQuery, as));
        return this;
    }

    public EntityQuery buildQuery(EntityQuery mainQuery) {
        mainQuery.setWith(this);
        return mainQuery;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public boolean isAsAfter() {
        return asAfter;
    }

    public List<LeftRight<EntityQuery, String>> getWiths() {
        return withs;
    }

}
