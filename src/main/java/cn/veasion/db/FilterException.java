package cn.veasion.db;

/**
 * FilterException
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class FilterException extends DbException {

    public FilterException(String message) {
        super(message);
    }

    public FilterException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FilterException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public void printStackTrace() {
        // super.printStackTrace();
    }

}
