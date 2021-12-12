package cn.veasion.db.model.vo;

import cn.veasion.db.model.po.CoursePO;

/**
 * CourseScoreVO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class CourseScoreVO extends CoursePO {

    private Integer avgScore;

    public Integer getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Integer avgScore) {
        this.avgScore = avgScore;
    }
}
