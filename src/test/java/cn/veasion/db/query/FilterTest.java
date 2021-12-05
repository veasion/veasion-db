package cn.veasion.db.query;

import cn.veasion.db.base.Filter;

/**
 * FilterTest
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class FilterTest {

    public static void main(String[] args) {
        Query query = new Query().eq("id", 1).gt("age", 18);

        query.addFilter(Filter.and());
        query.addFilters(Filter.leftBracket(), Filter.like("name", "xxx"), Filter.or(), Filter.like("code", "xxx"), Filter.rightBracket());
        query.eq("xxx", "xxx");

        query.getFilters().forEach(s -> System.out.print(s + " "));
        System.out.println();
        query.check();
        query.getFilters().forEach(s -> System.out.print(s + " "));
        System.out.println();
    }

}
