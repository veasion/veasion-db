package cn.veasion.db;

import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.utils.FieldUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * SimpleTempTest
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class SimpleTempTest {

    public static void main(String[] args) {
        Map<String, Method> getterMethodMap = FieldUtils.getterMethod(UserInfoPO.class);
        getterMethodMap.forEach((k, v) -> {
            System.out.println(k + ": " + v.getName());
        });
        System.out.println();
        Map<String, Method> setterMethodMap = FieldUtils.setterMethod(UserInfoPO.class);
        setterMethodMap.forEach((k, v) -> {
            System.out.println(k + ": " + v.getName());
        });
        System.out.println();
        FieldUtils.fieldColumns(UserInfoPO.class).forEach((k, v) -> {
            System.out.println(k + ": " + v);
        });
    }

}
