package cn.veasion.db;

import cn.veasion.db.query.Q;

/**
 * SecurityTest
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
public class SecurityTest extends BaseTest {

    public static void main(String[] args) {
        // 代码编写规范，防SQL注入

        // 规范一：field 字段必须写死或常量，不允许传参和有SQL相关代码

        // 错误示范
        String field = "name"; // 由接口传过来的参数，这样写会触发SQL注入
        println(studentDao.queryList(new Q().select(field).eq(field, "xxx")));

        // 正确示范
        println(studentDao.queryList(new Q().select("name").eq("name", "xxx")));

        // 规范二：表达式查询、表达式过滤、表达式更新有需要动态传值，不允许直接拼接，需要使用占位符

        // 错误示范
        String defaultValue = "18"; // 由接口传过来的默认值，这样写会触发SQL注入
        println(studentDao.query(new Q()
                .selectExpression("ifnull(age, " + defaultValue + ")", "age")
                .eq("id", 1)
        ));

        // 正确示范
        println(studentDao.query(new Q()
                .selectExpression("ifnull(age, #{value1})", "age", defaultValue)
                .eq("id", 1)
        ));

        // selectExpression / filterExpression / updateExpression

        // 规范三：表达式中使用字段需要写成 ${field}，它会默认替换成列名

        // 错误示范（会报错, desc 是关键字）
        try {
            println(studentDao.query(new Q().selectExpression("ifnull(desc, 'xxx')", null).page(1, 1)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 正确示范
        println(studentDao.query(new Q().selectExpression("ifnull(${desc}, 'xxx')", null).page(1, 1)));

    }

}
