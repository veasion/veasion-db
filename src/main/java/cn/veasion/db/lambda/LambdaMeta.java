package cn.veasion.db.lambda;

import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.TypeUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;

/**
 * LambdaMeta
 *
 * @author luozhuowei
 * @date 2022/11/29
 */
public class LambdaMeta implements Serializable {

    private String fieldName;
    private String implMethodName;
    private Class<?> instantiatedClass;

    public LambdaMeta(SerializedLambda serializedLambda) {
        try {
            Field capturingClassField = SerializedLambda.class.getDeclaredField("capturingClass");
            capturingClassField.setAccessible(true);
            Class<?> capturingClass = (Class<?>) capturingClassField.get(serializedLambda);
            String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
            String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/", ".");
            Class<?> instantiatedClass = TypeUtils.loadClass(instantiatedType, capturingClass.getClassLoader());
            this.implMethodName = serializedLambda.getImplMethodName();
            this.instantiatedClass = instantiatedClass;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        toFieldName();
    }

    public LambdaMeta(String implMethodName, Class<?> instantiatedClass) {
        this.implMethodName = implMethodName;
        this.instantiatedClass = instantiatedClass;
        toFieldName();
    }

    private void toFieldName() {
        if (implMethodName.startsWith("get") || implMethodName.startsWith("is") || implMethodName.startsWith("set")) {
            this.fieldName = FieldUtils.firstCase(implMethodName.substring(implMethodName.startsWith("is") ? 2 : 3), true);
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getImplMethodName() {
        return implMethodName;
    }

    public Class<?> getInstantiatedClass() {
        return instantiatedClass;
    }

    @Override
    public String toString() {
        return "LambdaMeta{" +
                "fieldName='" + fieldName + '\'' +
                ", implMethodName='" + implMethodName + '\'' +
                ", instantiatedClass=" + instantiatedClass +
                '}';
    }
}
