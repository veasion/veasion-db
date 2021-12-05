package cn.veasion.db.base;

import java.util.List;

/**
 * UserInfoPO
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
@Table(value = "t_user_info", autoIncrement = true)
public class UserInfoPO extends BasePO {

    private Integer id;

    public void setId(Integer id) {
        this.id = id;
    }

    private String username;
    private String userNike;
    private Integer age;
    private Integer version;

    @Column("test_column")
    private String test;
    @Column(ignore = true)
    private List<Long> ids;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserNike() {
        return userNike;
    }

    public void setUserNike(String userNike) {
        this.userNike = userNike;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "UserInfoPO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", userNike='" + userNike + '\'' +
                ", age=" + age +
                ", version=" + version +
                ", test='" + test + '\'' +
                ", ids=" + ids +
                '}';
    }
}
