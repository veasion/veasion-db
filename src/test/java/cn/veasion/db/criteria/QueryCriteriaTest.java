package cn.veasion.db.criteria;

import cn.veasion.db.BaseTest;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.TeacherPO;
import cn.veasion.db.model.vo.StudentVO;
import cn.veasion.db.query.EntityQuery;

import java.util.Arrays;
import java.util.HashMap;

/**
 * QueryCriteriaTest
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
public class QueryCriteriaTest extends BaseTest {

    public static void main(String[] args) {
        // QueryCriteria 设计思维是一套动态查询机制
        // 它提供注解提前定义字段查询方式和触发关联查询机制，动态触发关联查询或静态关联查询

        StudentInVO student;

        // 根据学号查询
        student = new StudentInVO();
        student.setSno("s001");
        queryList(student);

        // 根据ids集合查询
        student = new StudentInVO();
        student.setIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        queryList(student);

        // 根据班级名称查询
        student = new StudentInVO();
        student.setClassName("一班");
        student.setStartAge(15);
        student.setEndAge(20);
        queryList(student);

        // 根据班主任查询
        student = new StudentInVO();
        student.setTeacherName("罗");
        queryList(student);

        // 根据学号或名称查询
        student = new StudentInVO();
        student.setSnoOrName("熊");
        queryList(student);

        // 根据班级id区间查询
        student = new StudentInVO();
        student.setFilters(new HashMap<String, Object>() {{
            put("start_classId", 1);
            put("end_classId", 10);
        }});
        queryList(student);

        // 根据班级id区间查询（关联班级）
        student = new StudentInVO();
        student.setClassFilters(new HashMap<String, Object>() {{
            put("start_id", 1);
            put("end_id", 10);
        }});
        queryList(student);

        // 根据老师名称模糊查询（关联班级）
        student = new StudentInVO();
        student.setTeacherFilters(new HashMap<String, Object>() {{
            put("name", "罗%");
        }});
        queryList(student);

        // 通用查询
        CommonQueryCriteria commonQueryCriteria = new CommonQueryCriteria();
        commonQueryCriteria.addFilter("name", "熊%");
        println(studentDao.queryList(commonQueryCriteria));
    }

    private static void queryList(StudentInVO student) {
        QueryCriteriaConvert convert = new QueryCriteriaConvert(student);
        EntityQuery entityQuery = convert.getEntityQuery();
        entityQuery.selectAll();
        if (convert.hasJoin(ClassesPO.class)) {
            convert.getJoinEntityQuery(ClassesPO.class).select("className");
        }
        if (convert.hasJoin(TeacherPO.class)) {
            convert.getJoinEntityQuery(TeacherPO.class).select("name", "teacherName");
        }
        println(studentDao.queryList(entityQuery, StudentVO.class));
    }

}
