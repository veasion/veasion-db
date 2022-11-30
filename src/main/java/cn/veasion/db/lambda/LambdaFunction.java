package cn.veasion.db.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * LambdaFunction
 *
 * @author luozhuowei
 * @date 2022/11/29
 */
@FunctionalInterface
public interface LambdaFunction<T, R> extends Function<T, R>, Serializable {
}
