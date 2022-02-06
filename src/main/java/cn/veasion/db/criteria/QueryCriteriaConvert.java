package cn.veasion.db.criteria;

import cn.veasion.db.DbException;
import cn.veasion.db.FilterException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.jdbc.EntityDao;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.EntityQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;
import cn.veasion.db.utils.TypeUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * QueryCriteriaConvert
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
public class QueryCriteriaConvert {

    public static final Pattern FIELD_PATTERN = Pattern.compile("[_0-9a-zA-Z.]+");

    private Object object;
    private EntityQuery query;
    private JoinCriteria[] array;
    private List<LoadRelation> loadRelations;
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

    public boolean hasJoin(String tableName) {
        for (Class<?> clazz : joined) {
            if (tableName.equalsIgnoreCase(TypeUtils.getTableName(clazz))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取关联类型查询对象
     */
    public EntityQuery getJoinEntityQuery(Class<?> clazz) {
        return joinClassMap.get(clazz);
    }

    public EntityQuery getJoinEntityQuery(String tableName) {
        for (Map.Entry<Class<?>, EntityQuery> entry : joinClassMap.entrySet()) {
            if (tableName.equalsIgnoreCase(TypeUtils.getTableName(entry.getKey()))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<Class<?>, EntityQuery> getJoinClassMap() {
        return joinClassMap;
    }

    @SuppressWarnings("unchecked")
    private void handleFilters() {
        JoinCriteriaMulti joinCriteriaMulti = object.getClass().getAnnotation(JoinCriteriaMulti.class);
        if (joinCriteriaMulti != null) {
            array = joinCriteriaMulti.value();
        }
        initJoinClassMap();
        Map<String, Field> fields = FieldUtils.fields(object.getClass());
        for (Field field : fields.values()) {
            QueryCriteria queryCriteria = field.getAnnotation(QueryCriteria.class);
            AutoCriteria autoCriteria = field.getAnnotation(AutoCriteria.class);
            LoadRelation loadRelation = field.getAnnotation(LoadRelation.class);
            Object value = null;
            if (loadRelation != null) {
                if (loadRelation.value() == Void.class || "".equals(loadRelation.resultClassField())) {
                    throw new DbException("@LoadRelation 注解使用方式错误，字段: " + field.getName());
                }
                value = FieldUtils.getValue(object, field.getName(), true);
                if (value != null && !Boolean.FALSE.equals(value)) {
                    if (loadRelations == null) {
                        loadRelations = new ArrayList<>();
                    }
                    loadRelations.add(loadRelation);
                }
            }
            if (queryCriteria == null && autoCriteria == null) {
                continue;
            }
            if (value == null) {
                value = FieldUtils.getValue(object, field.getName(), true);
            }
            if (value == null) {
                continue;
            }
            if (queryCriteria != null) {
                handleQueryCriteria(field, queryCriteria, value);
            } else if (value instanceof Map) {
                handleAutoCriteria(autoCriteria, (Map<String, Object>) value);
            } else {
                handleAutoCriteria(autoCriteria, Collections.singletonMap(field.getName(), value));
            }
        }
    }

    private void handleAutoCriteria(AutoCriteria annotation, Map<String, Object> filters) {
        Class<?> relationClass = annotation.relation();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (annotation.skipEmpty() && isEmpty(value)) {
                continue;
            }
            if (!FIELD_PATTERN.matcher(key).matches() || key.length() > 30) {
                throw new FilterException("非法字段：" + key);
            }
            if (relationClass != Void.class) {
                checkJoin(relationClass);
            }
            Operator operator = Operator.EQ;
            if (value instanceof Collection || value instanceof Object[]) {
                operator = Operator.IN;
            } else if (key.startsWith("start_")) {
                key = key.substring(6);
                operator = Operator.GTE;
            } else if (key.startsWith("end_")) {
                key = key.substring(4);
                operator = Operator.LTE;
            } else if (value instanceof String &&
                    (String.valueOf(value).startsWith("%") || String.valueOf(value).endsWith("%"))) {
                operator = Operator.LIKE;
            }
            if (value instanceof Date) {
                value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
            }
            Filter filter = FilterUtils.getFilter(key, operator, value);
            if (relationClass != Void.class) {
                filter.fieldAs(joinClassMap.get(relationClass).getTableAs());
            }
            query.addFilter(filter);
        }
    }

    private void handleQueryCriteria(Field field, QueryCriteria annotation, Object value) {
        if (annotation.skipEmpty() && isEmpty(value)) {
            return;
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
                Filter filter = FilterUtils.getFilter(orFields[i], operator, value);
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
            Filter filter = FilterUtils.getFilter(fieldName, operator, value);
            if (relationClass != Void.class) {
                filter.fieldAs(joinClassMap.get(relationClass).getTableAs());
            }
            query.addFilter(filter);
        }
    }

    /**
     * 查询结果处理 @LoadRelation 注解，加载关联数据
     */
    public <E> void handleResultLoadRelation(EntityDao<?, ?> entityDao, List<E> list) {
        if (list == null || list.isEmpty()) return;
        Class<?> resultClass = list.get(0).getClass();
        Map<Field, Class<?>> fieldClassMap = loadResultRelation(resultClass);
        if (fieldClassMap.isEmpty()) return;
        for (Map.Entry<Field, Class<?>> entry : fieldClassMap.entrySet()) {
            Field field = entry.getKey();
            Class<?> clazz = entry.getValue();

            Map<Class<?>, EntityQuery> joinClassMap = new HashMap<>();
            Set<Class<?>> joined = new HashSet<>();
            initJoinClassMap(array, new EQ(this.query.getEntityClass(), "t"), joinClassMap);
            JoinCriteria joinCriteria = checkJoin(array, joinClassMap, joined, clazz);
            if (!joined.contains(clazz) || joinCriteria == null) {
                throw new DbException("@JoinCriteria 中未找到关联类：" + clazz.getName());
            }
            EntityQuery entityQuery = joinClassMap.get(joinCriteria.join());
            String[] onFields = joinCriteria.onFields();
            for (int i = 0; i < onFields.length; i += 2) {
                entityQuery.selectExpression("${" + onFields[i + 1] + "}", "mainField" + ((i + 2) / 2));
                List<Object> values = new ArrayList<>(list.size());
                for (E o : list) {
                    values.add(FieldUtils.getValue(o, onFields[i], true));
                }
                entityQuery.in(onFields[i + 1], values);
            }
            joinClassMap.get(clazz).selectAll();
            List<Map<String, Object>> resultList = entityDao.listForMap(entityQuery);
            StringBuilder sb = new StringBuilder();
            Map<String, List<Map<String, Object>>> keyList = new HashMap<>(resultList.size());
            for (Map<String, Object> map : resultList) {
                sb.setLength(0);
                for (int i = 0; i < onFields.length; i += 2) {
                    sb.append("_").append(map.get("mainField" + ((i + 2) / 2)));
                }
                keyList.compute(sb.toString(), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(map);
                    return v;
                });
            }
            try {
                for (E o : list) {
                    sb.setLength(0);
                    for (int i = 0; i < onFields.length; i += 2) {
                        sb.append("_").append(FieldUtils.getValue(o, onFields[i], true));
                    }
                    convert(o, field, keyList.get(sb.toString()));
                }
            } catch (Exception e) {
                throw new DbException("@JoinCriteria 字段类型赋值异常：" + field.getType().getName() + "." + field.getName(), e);
            }
        }
    }

    private void convert(Object obj, Field field, List<Map<String, Object>> values) throws Exception {
        Class<?> type = field.getType();
        Object val = null;
        if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            if (values == null || values.isEmpty()) {
                val = Set.class.isAssignableFrom(type) ? new HashSet<>() : new ArrayList<>();
            } else {
                List<Class<?>> classes = FieldUtils.fieldActualType(field);
                if (classes == null || classes.isEmpty()) {
                    val = Set.class.isAssignableFrom(type) ? new HashSet<>(values) : values;
                } else {
                    Collection<Object> list = Set.class.isAssignableFrom(type) ? new HashSet<>() : new ArrayList<>();
                    for (Map<String, Object> value : values) {
                        list.add(TypeUtils.map2Obj(value, classes.get(0)));
                    }
                    val = list;
                }
            }
        } else if (Map.class.isAssignableFrom(type)) {
            if (values != null && values.size() > 0) {
                if (values.size() > 1) {
                    throw new DbException("@LoadRelation 加载有多个对象：" + obj.getClass().getName() + "." + field.getName());
                }
                val = values.get(0);
            }
        } else if (values != null && values.size() > 0) {
            if (values.size() > 1) {
                throw new DbException("@LoadRelation 加载有多个对象：" + obj.getClass().getName() + "." + field.getName());
            }
            val = TypeUtils.map2Obj(values.get(0), type);
        }
        field.setAccessible(true);
        field.set(obj, val);
    }

    private Map<Field, Class<?>> loadResultRelation(Class<?> resultClass) {
        Map<Field, Class<?>> result = new HashMap<>();
        Map<String, Field> fields = FieldUtils.fields(resultClass);
        if (loadRelations != null) {
            for (LoadRelation loadRelation : loadRelations) {
                Field field = fields.get(loadRelation.resultClassField());
                if (field == null) {
                    throw new DbException("@LoadRelation 注解resultClassField字段" + loadRelation.resultClassField() + "在" + resultClass.getName() + "中不存在");
                }
                if (!joinClassMap.containsKey(loadRelation.value())) {
                    throw new DbException("@LoadRelation 注解 value = " + loadRelation.value().getName() + "未在类 @JoinCriteria 中定义");
                }
                result.put(field, loadRelation.value());
            }
        }
        for (Field field : fields.values()) {
            LoadRelation loadRelation = field.getAnnotation(LoadRelation.class);
            if (loadRelation != null) {
                if (loadRelation.value() == Void.class) {
                    throw new DbException("@LoadRelation注解使用错误：" + resultClass.getName() + "." + field.getName());
                }
                if (joinClassMap.containsKey(loadRelation.value())) {
                    result.put(field, loadRelation.value());
                }
            }
        }
        return result;
    }

    private void initJoinClassMap() {
        initJoinClassMap(array, query, joinClassMap);
        if (array != null) {
            for (JoinCriteria joinCriteria : array) {
                if (joinCriteria.staticJoin()) {
                    checkJoin(joinCriteria.join());
                }
            }
        }
    }

    private void checkJoin(Class<?> joinClass) {
        checkJoin(array, joinClassMap, joined, joinClass);
    }

    private static void initJoinClassMap(JoinCriteria[] array, EntityQuery query, Map<Class<?>, EntityQuery> joinClassMap) {
        if (array == null || array.length == 0) return;
        for (JoinCriteria joinCriteria : array) {
            Class<?> join = joinCriteria.join();
            if (join != Void.class) {
                joinClassMap.put(join, new EQ(join, FieldUtils.firstCase(join.getSimpleName(), true)));
            }
        }
        joinClassMap.put(Void.class, query);
    }

    private static JoinCriteria checkJoin(JoinCriteria[] array, Map<Class<?>, EntityQuery> joinClassMap, Set<Class<?>> joined, Class<?> joinClass) {
        if (joined.contains(joinClass)) {
            return null;
        }
        if (array == null || array.length == 0) {
            throw new DbException("@JoinCriteriaMulti 中关联类未找到：" + joinClass.getSimpleName());
        }
        JoinCriteria firstJoinCriteria = null;
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
                    firstJoinCriteria = joinCriteria;
                    break;
                }
            }
        } while (joinClass != Void.class && !joined.contains(joinClass));
        return firstJoinCriteria;
    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return "".equals(value);
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        return false;
    }

}
