package cn.veasion.db.criteria;

import cn.veasion.db.BaseTest;
import cn.veasion.db.model.vo.StudentVO;
import cn.veasion.db.query.EntityQuery;

import java.util.HashMap;
import java.util.List;

/**
 * LoadRelationTest
 *
 * @author luozhuowei
 * @date 2021/12/26
 */
public class LoadRelationTest extends BaseTest {

    public static void main(String[] args) {
        StudentInVO student = new StudentInVO();
        student.setFilters(new HashMap<String, Object>() {{
            put("name", "熊%");
        }});
        student.setLoadClasses(true);

        // classList 由 StudentInVO.class 查询类  loadClasses 字段上注解触发
        // teacherList 由 StudentVO.class 结果类 teacherList 字段上注解触发
        QueryCriteriaConvert convert = new QueryCriteriaConvert(student);
        EntityQuery entityQuery = convert.getEntityQuery();
        List<StudentVO> list = studentDao.queryList(entityQuery, StudentVO.class);
        convert.handleResultLoadRelation(studentDao, list);
        println(list);

    }

}
