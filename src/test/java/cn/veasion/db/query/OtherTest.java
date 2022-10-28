package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.base.Operator;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.update.EU;

/**
 * OtherTest
 *
 * @author luozhuowei
 * @date 2021/12/27
 */
public class OtherTest extends BaseTest {

    public static void main(String[] args) {
        println(studentDao.queryList(new EQ(StudentPO.class, "s")
                .select("id")
                .selectExpression("ifnull(${id}, ${sno})", "`key`")
                .filterExpression("id", Operator.EQ, "${id} + 1")
                .sqlFilter(handler -> handler.asField("id") + " > ?", 0)
        ));
        studentDao.update(new EU(StudentPO.class, "t")
                .updateExpression("id", "${id}")
                .filterExpression("id", Operator.EQ, "${id}")
                .sqlFilter(handler -> handler.asField("id") + " < 5")
        );
    }

}
