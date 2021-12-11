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
public abstract class AbstractUpdate<T> extends AbstractFilter<T> {

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

    public T updateExpression(String field, Expression expression) {
        updates.put(handleField(field), Objects.requireNonNull(expression));
        return getSelf();
    }

    public T excludeUpdates(String... fields) {
        if (excludeUpdates == null) excludeUpdates = new HashSet<>();
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
