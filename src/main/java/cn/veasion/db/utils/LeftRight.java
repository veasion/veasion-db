package cn.veasion.db.utils;

/**
 * LeftRight
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class LeftRight<L, R> {

    private L left;
    private R right;

    public LeftRight(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> LeftRight<L, R> build(L left, R right) {
        return new LeftRight<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }
}
