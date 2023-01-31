package cn.veasion.db.criteria;

import cn.veasion.db.query.OrderParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonQueryCriteria
 *
 * @author luozhuowei
 * @date 2023/1/7
 */
public class CommonQueryCriteria {

    @AutoCriteria
    private Map<String, Object> filters = new HashMap<>();

    private Integer page;
    private Integer size;

    private List<OrderParam> orders;

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Integer getPage() {
        return page;
    }

    public CommonQueryCriteria setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public CommonQueryCriteria setSize(Integer size) {
        this.size = size;
        return this;
    }

    public void setOrder(OrderParam orderParam) {
        if (orderParam != null) {
            if (orders == null) {
                orders = new ArrayList<>();
            }
            orders.add(orderParam);
        }
    }

    public List<OrderParam> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderParam> orders) {
        this.orders = orders;
    }

    public void withLike(String key) {
        withLike(key, true, true);
    }

    public void withLike(String key, boolean left, boolean right) {
        Object v;
        if (filters != null && (v = filters.get(key)) != null) {
            String value = v.toString().trim();
            if ("".equals(value)) {
                return;
            }
            if (left && !value.startsWith("%")) {
                value = "%" + value;
            }
            if (right && !value.endsWith("%")) {
                value += "%";
            }
            filters.put(key, value);
        }
    }

    public CommonQueryCriteria addFilter(String key, Object value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(key, value);
        return this;
    }

    public CommonQueryCriteria gt(String key, Object value) {
        return addFilter("gt_" + key, value);
    }

    public CommonQueryCriteria gte(String key, Object value) {
        return addFilter("gte_" + key, value);
    }

    public CommonQueryCriteria lt(String key, Object value) {
        return addFilter("lt_" + key, value);
    }

    public CommonQueryCriteria lte(String key, Object value) {
        return addFilter("lte_" + key, value);
    }

    public CommonQueryCriteria neq(String key, Object value) {
        return addFilter("neq_" + key, value);
    }

    public Object remove(String key) {
        if (filters != null) {
            return filters.remove(key);
        }
        return null;
    }

}
