package cn.veasion.db.model.enums;

import cn.veasion.db.interceptor.IEnum;

/**
 * 性别枚举
 *
 * @author luozhuowei
 * @date 2022/12/1
 */
public enum SexEnum implements IEnum<Integer> {

    NONE(0, "保密"),

    MALE(1, "男"),

    FEMALE(2, "女");

    private Integer value;
    private String desc;

    SexEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}
