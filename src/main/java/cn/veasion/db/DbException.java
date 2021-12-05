package cn.veasion.db;

/**
 * DbException
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class DbException extends RuntimeException {

    public DbException(String message) {
        super(message);
    }

    public DbException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DbException(Throwable throwable) {
        super(throwable);
    }

}
