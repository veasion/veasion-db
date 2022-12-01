package cn.veasion.db.lambda;

import cn.veasion.db.DbException;
import cn.veasion.db.utils.TypeUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SerializedLambdaUtils
 *
 * @author luozhuowei
 * @date 2022/11/29
 */
public class SerializedLambdaUtils {

    private static final Map<Serializable, LambdaMeta> LAMBDA_META_CACHE = new ConcurrentHashMap<>();

    public static <T, R> LambdaMeta getLambdaMeta(LambdaFunction<T, R> lambdaFunction) {
        return getLambdaMeta((Serializable) lambdaFunction);
    }

    public static LambdaMeta getLambdaMeta(Serializable serializable) {
        if (LAMBDA_META_CACHE.containsKey(serializable)) {
            return LAMBDA_META_CACHE.get(serializable);
        }
        try {
            LambdaMeta lambdaMeta;
            if (serializable instanceof Proxy) {
                lambdaMeta = getLambdaMetaByProxy((Proxy) serializable);
            } else {
                try {
                    Method method = serializable.getClass().getDeclaredMethod("writeReplace");
                    method.setAccessible(true);
                    lambdaMeta = new LambdaMeta((SerializedLambda) method.invoke(serializable));
                } catch (Throwable t) {
                    lambdaMeta = invoke(serializable);
                }
            }
            LAMBDA_META_CACHE.put(serializable, lambdaMeta);
            return lambdaMeta;
        } catch (DbException e) {
            throw e;
        } catch (Throwable e) {
            throw new DbException("获取lambda表达式信息失败：" + serializable.toString(), e);
        }
    }

    private static LambdaMeta invoke(Serializable serializable) throws Exception {
        Object object = TypeUtils.serializableCopy(serializable);
        Method method = object.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return new LambdaMeta((SerializedLambda) method.invoke(object));
    }

    private static LambdaMeta getLambdaMetaByProxy(Proxy proxy) throws Throwable {
        InvocationHandler handler = Proxy.getInvocationHandler(proxy);
        Field val$targetField = handler.getClass().getDeclaredField("val$target");
        val$targetField.setAccessible(true);
        Object dmh = val$targetField.get(handler);
        Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
        Field memberField = classDirectMethodHandle.getDeclaredField("member");
        memberField.setAccessible(true);
        Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
        Field clazzField = classMemberName.getDeclaredField("clazz");
        clazzField.setAccessible(true);
        Field nameField = classMemberName.getDeclaredField("name");
        nameField.setAccessible(true);
        Object member = memberField.get(dmh);
        String name = (String) nameField.get(member);
        Class<?> clazz = (Class<?>) clazzField.get(member);
        return new LambdaMeta(name, clazz);
    }

}
