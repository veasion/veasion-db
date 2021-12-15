package cn.veasion.db.criteria;

import cn.veasion.db.DbException;
import cn.veasion.db.FilterException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.EntityQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.utils.FieldUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * QueryCriteriaConvert
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
public class QueryCriteriaConvert {

    private Object object;
    private EntityQuery query;
    private JoinCriteria[] array;
    private Set<Class<?>> joined = new HashSet<>();
    private Map<Class<?>, EntityQuery> joinClassMap = new HashMap<>();

    public QueryCriteriaConvert(Object object) {
        this(object, null);
    }

    public QueryCriteriaConvert(Object object, Class<?> entityClass) {
        Objects.requireNonNull(object, "参数不能为空");
        this.object = object;
        this.query = new EQ(entityClass, "t");
        this.handleFilters();
    }

    /**
     * 获取查询对象
     */
    public EntityQuery getEntityQuery() {
        return this.query;
    }

    /**
     * 判断类型是否存在关联
     */
    public boolean hasJoin(Class<?> clazz) {
        return joined.contains(clazz);
    }

    /**
     * 获取关联类型查询对象
     */
    public EntityQuery getJoinEntityQuery(Class<?> clazz) {
        return joinClassMap.get(clazz);
    }

    public Map<Class<?>, EntityQuery> getJoinClassMap() {
        return joinClassMap;
    }

    private void handleFilters() {
        JoinCriteriaMulti joinCriteriaMulti = object.getClass().getAnnotation(JoinCriteriaMulti.class);
        if (joinCriteriaMulti != null) {
            array = joinCriteriaMulti.value();
        }
        initJoinClassMap();
        Map<String, Field> fields = FieldUtils.fields(object.getClass());
        for (Field field : fields.values()) {
            QueryCriteria annotation = field.getAnnotation(QueryCriteria.class);
            if (annotation == null) {
                continue;
            }
            Object value = FieldUtils.getValue(object, field.getName(), true);
            if (value == null) {
                continue;
            }
            boolean skipEmpty = annotation.skipEmpty();
            if (skipEmpty) {
                if (value instanceof String && "".equals(value)) {
                    continue;
                } else if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                    continue;
                } else if (value instanceof Object[] && ((Object[]) value).length == 0) {
                    continue;
                }
            }
            String fieldName = "".equals(annotation.field()) ? field.getName() : annotation.field();
            Operator operator = annotation.value();
            Class<?> relationClass = annotation.relation();

            if (relationClass != Void.class) {
                checkJoin(relationClass);
            }

            String[] orFields = annotation.orFields();
            if (orFields.length > 0) {
                query.addFilter(Filter.leftBracket());
                for (int i = 0; i < orFields.length; i++) {
                    Filter filter = getFilter(orFields[i], operator, value);
                    if (relationClass != Void.class) {
                        filter.fieldAs(joinClassMap.get(relationClass).getTableAs());
                    }
                    query.addFilter(filter);
                    if (i < orFields.length - 1) {
                        query.addFilter(Filter.or());
                    }
                }
                query.addFilter(Filter.rightBracket());
            } else {
                Filter filter = getFilter(fieldName, operator, value);
                if (relationClass != Void.class) {
                    filter.fieldAs(joinClassMap.get(relationClass).getTableAs());
                }
                query.addFilter(filter);
            }
        }
    }

    private void initJoinClassMap() {
        if (array == null || array.length == 0) return;
        for (JoinCriteria joinCriteria : array) {
            Class<?> join = joinCriteria.join();
            if (join != Void.class) {
                joinClassMap.put(join, new EQ(join, FieldUtils.firstCase(join.getSimpleName(), true)));
            }
        }
        joinClassMap.put(Void.class, query);
        for (JoinCriteria joinCriteria : array) {
            if (joinCriteria.staticJoin()) {
                checkJoin(joinCriteria.join());
            }
        }
    }

    private void checkJoin(Class<?> joinClass) {
        if (joined.contains(joinClass)) {
            return;
        }
        if (array == null || array.length == 0) {
            throw new DbException("@JoinCriteriaMulti 中关联类未找到：" + joinClass.getSimpleName());
        }
        Set<JoinCriteria> joinCriteriaSet = new HashSet<>(Arrays.asList(array));
        do {
            for (JoinCriteria joinCriteria : joinCriteriaSet) {
                if (joinClass == joinCriteria.join()) {
                    EntityQuery main = joinClassMap.get(joinCriteria.value());
                    EntityQuery join = joinClassMap.get(joinCriteria.join());
                    JoinQueryParam joinQueryParam = main.join(joinCriteria.joinType(), join);
                    joined.add(joinClass);
                    String[] onFields = joinCriteria.onFields();
                    for (int i = 0; i < onFields.length; i += 2) {
                        joinQueryParam.on(onFields[i], onFields[i + 1]);
                    }
                    joinClass = joinCriteria.value();
                    joinCriteriaSet.remove(joinCriteria);
                    break;
                }
            }
        } while (joinClass != Void.class && !joined.contains(joinClass));
    }

    public static Filter getFilter(String field, Operator operator, Object value) {
        if (Operator.EQ.equals(operator)) {
            return Filter.eq(field, value);
        } else if (Operator.NEQ.equals(operator)) {
            return Filter.neq(field, value);
        } else if (Operator.GT.equals(operator)) {
            return Filter.gt(field, value);
        } else if (Operator.GTE.equals(operator)) {
            return Filter.gte(field, value);
        } else if (Operator.LT.equals(operator)) {
            return Filter.lt(field, value);
        } else if (Operator.LTE.equals(operator)) {
            return Filter.lte(field, value);
        } else if (Operator.IN.equals(operator)) {
            if (value instanceof Collection) {
                return Filter.in(field, (Collection<?>) value);
            } else if (value instanceof Object[]) {
                return Filter.in(field, (Object[]) value);
            } else {
                throw new FilterException(field + " 字段 Operator.IN 类型必须是集合或者数组");
            }
        } else if (Operator.NOT_IN.equals(operator)) {
            if (value instanceof Collection) {
                return Filter.notIn(field, (Collection<?>) value);
            } else if (value instanceof Object[]) {
                return Filter.notIn(field, (Object[]) value);
            } else {
                throw new FilterException(field + " 字段 Operator.IN 类型必须是集合或者数组");
            }
        } else if (Operator.LIKE.equals(operator)) {
            return Filter.like(field, value);
        } else if (Operator.BETWEEN.equals(operator)) {
            if (value instanceof Collection) {
                Iterator<?> iterator = ((Collection<?>) value).iterator();
                return Filter.between(field, iterator.next(), iterator.next());
            } else if (value instanceof Object[]) {
                Object[] objects = (Object[]) value;
                return Filter.between(field, objects[0], objects[1]);
            } else {
                throw new FilterException(field + " 字段 Operator.BETWEEN 类型必须是集合或者数组");
            }
        } else if (Operator.NULL.equals(operator)) {
            return Filter.isNull(field);
        } else if (Operator.NOT_NULL.equals(operator)) {
            return Filter.isNotNull(field);
        } else {
            throw new FilterException(field + " 不支持 Operator." + operator.name());
        }
    }

}
