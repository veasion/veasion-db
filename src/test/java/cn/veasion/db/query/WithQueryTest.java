package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.TableEntity;
import cn.veasion.db.model.po.AreaPO;

/**
 * WithQueryTest
 *
 * @author luozhuowei
 * @date 2022/11/8
 */
public class WithQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 通过 with 联合查询
        // with area1 as (select * from t_area where level = 1),
        // area2 as (select * from t_area where level = 2)
        // select * from area1
        // union all
        // select * from area2
        EntityQuery entityQuery = With.build()
                .with(new EQ(AreaPO.class).eq("level", 1), "area1")
                .with(new EQ(AreaPO.class).eq("level", 2), "area2")
                .buildQuery(
                        new EQ(new TableEntity("area1")).unionAll(new EQ(new TableEntity("area2")))
                );
        println(areaDao.queryList(entityQuery));

        // 通过 with 递归查询
        // with recursive area as (
        //   select * from t_area where code = '310000'
        //   union all
        //   select t1.* from t_area t1 join area t2 on t1.parent_code = t2.code
        // )
        // select * from area
        EntityQuery t1 = new EQ(AreaPO.class, "t1");
        EntityQuery t2 = new EQ(new TableEntity("area"), "t2");
        t1.join(t2).on("parentCode", "code");
        t1.selectAll();

        EntityQuery entityQuery2 = With.buildRecursive()
                .with(new EQ(AreaPO.class).eq("code", "310000").unionAll(t1), "area")
                .buildQuery(new EQ(new TableEntity("area")));
        println(areaDao.queryList(entityQuery2));

        // clickhouse with 查询
        // with (select 1) as t1, (select 2) as t2 select t1, t2
        // println(areaDao.queryList(With.buildAsAfter().with(new EQ(new TableEntity(null)).select("1"), "t1").with(new EQ(new TableEntity(null)).select("2"), "t2").buildQuery(new EQ(new TableEntity(null)).selects("t1", "t2"))));
    }

}
