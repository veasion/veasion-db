package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.jdbc.UpdateSQL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * AbstractUpdate
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public abstract class AbstractUpdate<T extends AbstractUpdate<?>> extends AbstractFilter<T> {

    private boolean skipNullField;
    private Set<String> excludeUpdates;
    private Map<String, Object> updates = new HashMap<>();

    public T skipNullField() {
        this.skipNullField = true;
        return getSelf();
    }

    public T update(String field, Object value) {
        if (skipNullField && value == null) {
            return getSelf();
        }
        field = handleField(field);
        if (excludeUpdates != null && excludeUpdates.contains(field)) {
            return getSelf();
        }
        updates.put(field, value);
        return getSelf();
    }

    /**
     * 表达式更新
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成对象字段和values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：${version} + 1 <br>
     *                   示例二：${totalAmount} + #{totalAmount} <br>
     *                   示例三：${id} + #{value1} + #{value2} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public T updateExpression(String field, String expression, Object... values) {
        return updateExpression(field, Expression.update(expression, values));
    }

    public T updateExpression(String field, Expression expression) {
        updates.put(handleField(field), Objects.requireNonNull(expression));
        return getSelf();
    }

    public T excludeUpdates(String... fields) {
        if (excludeUpdates == null) {
            excludeUpdates = new HashSet<>();
        }
        for (String field : fields) {
            excludeUpdates.add(handleField(field));
        }
        return getSelf();
    }

    public boolean isSkipNullField() {
        return skipNullField;
    }

    public Map<String, Object> getUpdates() {
        return updates;
    }

    protected abstract String handleField(String field);

    @Override
    public void check(Class<?> mainEntityClass) {
        super.check(mainEntityClass);
        if (skipNullField || excludeUpdates != null) {
            List<String> removes = new ArrayList<>();
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (skipNullField && entry.getValue() == null) {
                    removes.add(entry.getKey());
                } else if (excludeUpdates != null && excludeUpdates.contains(entry.getKey())) {
                    removes.add(entry.getKey());
                }
            }
            if (removes.size() > 0) {
                removes.forEach(updates::remove);
            }
        }
    }

    public Set<String> getExcludeUpdates() {
        return excludeUpdates;
    }

    public UpdateSQL sqlValue() {
        return UpdateSQL.build(this);
    }

}
