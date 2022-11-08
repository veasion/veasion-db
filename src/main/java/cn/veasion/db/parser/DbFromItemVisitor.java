package cn.veasion.db.parser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DbFromItemVisitor
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class DbFromItemVisitor implements FromItemVisitor {

    Table table;
    String var;
    String tableName;
    boolean isSubQuery;
    Set<String> withAs;
    StringBuilder sb = new StringBuilder();

    public static final ThreadLocal<Map<String, AtomicInteger>> NAME_INDEX = ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Override
    public void visit(Table table) {
        this.table = table;
        tableName = SQLParseUtils.sqlTrim(table.getName());
        String tableAs = table.getAlias() != null ? table.getAlias().getName() : null;
        if (var == null) {
            var = SQLParseUtils.getVarByTable(table);
        }
        if (var == null) {
            var = tableAs;
        }
        var = checkVar(var);
        sb.append("\r\n");
        sb.append("EQ ").append(var).append(" = new EQ(");
        if (withAs != null && withAs.contains(tableName)) {
            sb.append("new TableEntity(\"").append(tableName).append("\")");
        } else {
            sb.append(SQLParseUtils.getTableClass(tableName));
        }
        if (tableAs != null) {
            sb.append(", \"").append(tableAs).append("\"");
        }
        sb.append(");\r\n");
    }

    @Override
    public void visit(SubSelect subSelect) {
        isSubQuery = true;
        var = checkVar(subSelect.getAlias().getName());
        String subVar = SubSelectVisitor.visit(sb, subSelect);
        sb.append("\r\n");
        sb.append("SubQuery ").append(var).append(" = new SubQuery(");
        sb.append(subVar).append(", \"").append(subSelect.getAlias().getName()).append("\");\r\n");
    }

    private String checkVar(String var) {
        Map<String, AtomicInteger> map = NAME_INDEX.get();
        AtomicInteger value = map.compute(var, (k, v) -> {
            if (v == null) {
                v = new AtomicInteger(0);
            } else {
                v.incrementAndGet();
            }
            return v;
        });
        if (value.get() == 0) {
            return var;
        } else {
            return var + value.get();
        }
    }

    @Override
    public void visit(SubJoin subJoin) {
        System.out.println("subJoin: " + subJoin);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        System.out.println("lateralSubSelect: " + lateralSubSelect);
    }

    @Override
    public void visit(ValuesList valuesList) {
        System.out.println("valuesList: " + valuesList);
    }

    @Override
    public void visit(TableFunction tableFunction) {
        System.out.println("tableFunction: " + tableFunction);
    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {
        System.out.println("parenthesisFromItem: " + parenthesisFromItem);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
