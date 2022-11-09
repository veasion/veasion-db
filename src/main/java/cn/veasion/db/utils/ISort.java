package cn.veasion.db.utils;

/**
 * ISort
 *
 * @author luozhuowei
 * @date 2022/11/9
 */
public interface ISort {

    /**
     * 排序，值越大越优先
     */
    default int sort() {
        return 0;
    }

}
