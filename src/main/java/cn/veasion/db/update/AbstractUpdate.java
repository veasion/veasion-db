package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.jdbc.DaoUtils;
import cn.veasion.db.utils.LeftRight;

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
@SuppressWarnings("unchecked")
public abstract class AbstractUpdate<T> extends AbstractFilter<T> {

    private boolean skipNullField;
    private Class<?> entityClass;
    private Set<String> excludeUpdates;
    private Map<String, Object> updates = new HashMap<>();

    public T skipNullField() {
        this.skipNullField = true;
        return (T) this;
    }

    public T update(String field, Object value) {
        if (skipNullField && value == null) {
            return (T) this;
        }
        field = handleField(field);
        if (excludeUpdates != null && excludeUpdates.contains(field)) {
            return (T) this;
        }
        updates.put(field, value);
        return (T) this;
    }

    public T updateExpression(String field, Expression expression) {
        updates.put(handleField(field), Objects.requireNonNull(expression));
        return (T) this;
    }

    public T excludeUpdates(String... fields) {
        if (excludeUpdates == null) excludeUpdates = new HashSet<>();
        for (String field : fields) {
            excludeUpdates.add(handleField(field));
        }
        return (T) this;
    }

    public boolean isSkipNullField() {
        return skipNullField;
    }

    public Map<String, Object> getUpdates() {
        return updates;
    }

    protected abstract String handleField(String field);

    @Override
    public void check() {
        super.check();
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

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public LeftRight<String, Object[]> sqlValue() {
        return DaoUtils.update(this);
    }

}
