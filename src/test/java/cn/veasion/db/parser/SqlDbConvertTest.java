package cn.veasion.db.parser;

/**
 * SqlDbConvertTest
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class SqlDbConvertTest {

    public static void main(String[] args) {
        String sql = "select *, user_name as sss, age, count(id) as c from dd.`t_student` t" +
                " where t.age > 0 and t.id in (1,2,3) and (name like '%sss%' or name like 'ss%')" +
                " and id <= 0 and user_age <> ifnull(age, 0) and `desc` is not null";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "select s.*, t.name from t_student s, t_classes c left join t_user u " +
                "on c.id = u.id and c.id > s.age and c.is_deleted = 0" +
                "where s.class_id = c.id and c.id > 0 group by s.user_id, c.id " +
                "having count(s.id) > 1 and avg(c.age) >= 18 order by c.create_time desc, s.id " +
                "limit 10, 10";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "select id, name, (select age from t_classes where id = t.class_id) as '_age' " +
                "from (select id, name from t_student where id > 0) t " +
                "where t.id in (select id from t_classes where id > 50) and exists (select 1 from t_user) " +
                "union select id, name, age from t_user where id > 0 order by id desc limit 10";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "select * from t_score where ifnull(score, 0) = score + 0";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "replace into t_student(id, name, create_time) values(1, 'test', now())";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "insert into t_student(id, name, create_time) select id, name, now() from t_classes where id > 0";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "delete from t_student where id > 0 and age = ifnull(id, 1)";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
        System.out.println("===============================");

        sql = "update t_student s, t_classes c set age = 18, c.name = 'sss', c.age = now() where s.class_id = c.id and s.id > 0 and c.id < 10";
        System.out.println(sql);
        System.out.println();
        System.out.println(SQLParseUtils.parseSQLConvert(sql));
    }

}
