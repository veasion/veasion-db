package cn.veasion.db.interceptor;

import cn.veasion.db.base.Expression;

/**
 * 逻辑删除拦截器
 *
 * @author luozhuowei
 * @date 2022/11/8
 */
public class MyLogicDeleteInterceptor extends cn.veasion.db.interceptor.LogicDeleteInterceptor {

    public MyLogicDeleteInterceptor() {
        // super("isDeleted", 0, 1);
        super("isDeleted", 0, Expression.update("${id}"));
    }

    @Override
    protected boolean containSkipClass(Class<?> clazz) {
        if (!ILogicDelete.class.isAssignableFrom(clazz)) {
            return true;
        }
        return super.containSkipClass(clazz);
    }

}