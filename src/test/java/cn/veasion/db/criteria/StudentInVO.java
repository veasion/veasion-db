package cn.veasion.db.criteria;

import cn.veasion.db.base.Operator;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.TeacherPO;

import java.util.List;
import java.util.Map;

/**
 * StudentInVO
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
@JoinCriteriaMulti({
        @JoinCriteria(join = ClassesPO.class, onFields = {"classId", "id"}),
        @JoinCriteria(value = ClassesPO.class, join = TeacherPO.class, onFields = {"masterTno", "tno"})
})
public class StudentInVO {

    // 简单字段查询映射
    @QueryCriteria
    private String sno;
    @QueryCriteria(value = Operator.IN, field = "id")
    private List<Long> ids;
    @QueryCriteria(value = Operator.GTE, field = "age")
    private Integer startAge;
    @QueryCriteria(value = Operator.LTE, field = "age")
    private Integer endAge;
    @QueryCriteria(Operator.LIKE)
    private String name;
    @QueryCriteria(value = Operator.LIKE, orFields = {"sno", "name"})
    private String snoOrName;
    @QueryCriteria(value = Operator.LIKE, relation = ClassesPO.class)
    private String className;
    @QueryCriteria(value = Operator.LIKE, field = "name", relation = TeacherPO.class)
    private String teacherName;

    // 通用查询映射
    @AutoCriteria
    private Map<String, Object> filters;
    @AutoCriteria(relation = ClassesPO.class)
    private Map<String, Object> classFilters;
    @AutoCriteria(relation = TeacherPO.class)
    private Map<String, Object> teacherFilters;

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Integer getStartAge() {
        return startAge;
    }

    public void setStartAge(Integer startAge) {
        this.startAge = startAge;
    }

    public Integer getEndAge() {
        return endAge;
    }

    public void setEndAge(Integer endAge) {
        this.endAge = endAge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnoOrName() {
        return snoOrName;
    }

    public void setSnoOrName(String snoOrName) {
        this.snoOrName = snoOrName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getClassFilters() {
        return classFilters;
    }

    public void setClassFilters(Map<String, Object> classFilters) {
        this.classFilters = classFilters;
    }

    public Map<String, Object> getTeacherFilters() {
        return teacherFilters;
    }

    public void setTeacherFilters(Map<String, Object> teacherFilters) {
        this.teacherFilters = teacherFilters;
    }
}

