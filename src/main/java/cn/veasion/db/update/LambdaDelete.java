package cn.veasion.db.update;

import cn.veasion.db.lambda.ILambdaFilter;

/**
 * LambdaDelete
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public class LambdaDelete<E> extends Delete implements ILambdaFilter<Delete, E> {

    @Override
    protected LambdaDelete<E> getSelf() {
        return this;
    }

}
