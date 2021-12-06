package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractUpdate
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
@SuppressWarnings("unchecked")
public abstract class AbstractUpdate<T> extends AbstractFilter<T> {

    private boolean skipNullField;
    private Map<String, Object> updates;

    public T skipNullField() {
        return (T) this;
    }

    public T update(String field, Object value) {
        if (skipNullField && value == null) {
            return (T) this;
        }
        updates.put(field, value);
        return (T) this;
    }

    public T updateExpression(String field, Expression expression) {
        updates.put(field, Objects.requireNonNull(expression));
        return (T) this;
    }

    public boolean isSkipNullField() {
        return skipNullField;
    }

    public Map<String, Object> getUpdates() {
        return updates;
    }

    @Override
    public void check() {
        super.check();
        if (skipNullField) {
            List<String> removes = new ArrayList<>();
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (entry.getValue() == null) {
                    removes.add(entry.getKey());
                }
            }
            if (removes.size() > 0) {
                removes.forEach(updates::remove);
            }
        }
    }

}
