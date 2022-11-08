package cn.veasion.db.parser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SelectStatementVisitor
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class DbSelectVisitor implements SelectVisitor {

    String var;
    Set<String> withAs;
    StringBuilder sb = new StringBuilder();

    @Override
    public void visit(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        DbFromItemVisitor dbFromItemVisitor = new DbFromItemVisitor();
        dbFromItemVisitor.var = var;
        dbFromItemVisitor.withAs = withAs;
        fromItem.accept(dbFromItemVisitor);
        var = dbFromItemVisitor.var;
        sb.append(dbFromItemVisitor.sb);
        joins(plainSelect.getJoins());
        if (plainSelect.getDistinct() != null) {
            sb.append(var);
            appendLine(".distinct();");
        }
        handleSelectItems(var, plainSelect.getSelectItems());
        Expression where = plainSelect.getWhere();
        if (where != null) {
            DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
            expressionVisitor.var = var;
            expressionVisitor.master = sb;
            where.accept(expressionVisitor);
            sb.append(expressionVisitor.sb);
        }
        List<Expression> groups = plainSelect.getGroupByColumnReferences();
        if (groups != null && !groups.isEmpty()) {
            sb.append(var).append(".groupBy(");
            int len = sb.length();
            for (Expression group : groups) {
                if (group instanceof Column) {
                    String columnField = SQLParseUtils.getColumnField(var, (Column) group);
                    sb.append("\"").append(columnField).append("\"").append(", ");
                }
            }
            if (sb.length() > len) {
                sb.setLength(sb.length() - 2);
            }
            sb.append(");\r\n");
        }
        Expression having = plainSelect.getHaving();
        if (having != null) {
            DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
            expressionVisitor.var = var;
            expressionVisitor.master = sb;
            expressionVisitor.havingFilter = true;
            having.accept(expressionVisitor);
            sb.append(expressionVisitor.sb);
        }
        orderBy(sb, var, plainSelect.getOrderByElements());
        limit(sb, var, plainSelect.getLimit());
    }

    private void joins(List<Join> joins) {
        if (joins == null || joins.isEmpty()) return;
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            Expression onExpression = join.getOnExpression();
            DbFromItemVisitor joinFromItemVisitor = new DbFromItemVisitor();
            joinFromItemVisitor.withAs = withAs;
            rightItem.accept(joinFromItemVisitor);
            sb.append(joinFromItemVisitor.sb);
            String joinType = "join";
            if (join.isLeft()) {
                joinType = "leftJoin";
            } else if (join.isRight()) {
                joinType = "rightJoin";
            } else if (join.isFull()) {
                joinType = "fullJoin";
            }
            sb.append(var).append(".").append(joinType).append("(").append(joinFromItemVisitor.var).append(")");
            if (onExpression != null) {
                DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
                expressionVisitor.master = sb;
                expressionVisitor.onFilter = true;
                expressionVisitor.joinVar = joinFromItemVisitor.var;
                expressionVisitor.joinUseMainVar = var;
                onExpression.accept(expressionVisitor);
                sb.append(expressionVisitor.sb);
            }
            sb.append(";\r\n");
        }
    }

    private void orderBy(StringBuilder sb, String var, List<OrderByElement> orders) {
        if (orders != null && !orders.isEmpty()) {
            for (OrderByElement order : orders) {
                Expression expression = order.getExpression();
                if (expression instanceof Column) {
                    if (order.isAsc()) {
                        sb.append(var).append(".asc(");
                    } else {
                        sb.append(var).append(".desc(");
                    }
                    sb.append("\"").append(SQLParseUtils.getColumnField(var, (Column) expression)).append("\"");
                    sb.append(");\r\n");
                }
            }
        }
    }

    private void limit(StringBuilder sb, String var, Limit limit) {
        if (limit == null) return;
        Expression offset = limit.getOffset();
        Expression rowCount = limit.getRowCount();
        if (rowCount instanceof LongValue && (offset == null || offset instanceof LongValue)) {
            long size = ((LongValue) rowCount).getValue();
            sb.append(var).append(".page(");
            if (offset == null) {
                sb.append("1");
            } else {
                sb.append((int) (size / ((LongValue) offset).getValue()));
            }
            sb.append(", ");
            sb.append(size);
            sb.append(");\r\n");
        } else {
            sb.append("// 分页示例\r\n");
            sb.append("// ").append(var).append(".page(1, 10);\r\n");
        }
    }

    private void handleSelectItems(String var, List<SelectItem> selectItems) {
        if (selectItems == null || selectItems.isEmpty()) return;
        SelectItemVisitor itemVisitor = new SelectItemVisitor() {
            @Override
            public void visit(AllColumns allColumns) {
                sb.append(var).append(".selectAll();");
                appendLine();
            }

            @Override
            public void visit(AllTableColumns allTableColumns) {
                String _var = var;
                Table table = allTableColumns.getTable();
                if (table != null && !"".equals(table.toString())) {
                    _var = SQLParseUtils.getVarByTable(table);
                }
                sb.append(_var).append(".selectAll();");
                appendLine();
            }

            @Override
            public void visit(SelectExpressionItem selectExpressionItem) {
                String _var = var;
                Expression expression = selectExpressionItem.getExpression();
                Alias alias = selectExpressionItem.getAlias();
                String as = null;
                if (alias != null && alias.getName() != null) {
                    as = SQLParseUtils.sqlTrim(alias.getName().replace(" AS ", "").trim());
                }
                if (expression instanceof Column) {
                    Table table = ((Column) expression).getTable();
                    if (table != null && !"".equals(table.toString())) {
                        _var = SQLParseUtils.getVarByTable(table);
                    }
                    String columnName = ((Column) expression).getColumnName();
                    sb.append(_var).append(".select(\"").append(SQLParseUtils.columnToField(columnName)).append("\"");
                } else if (expression instanceof Function) {
                    sb.append(_var).append(".selectExpression(\"").append(expression.toString()).append("\"");
                    if (alias == null || alias.getName() == null) {
                        sb.append(", null");
                    }
                } else if (expression instanceof SubSelect) {
                    String subVar = SubSelectVisitor.visit(sb, (SubSelect) expression, as);
                    sb.append(_var).append(".selectSubQuery(SubQueryParam.build(").append(subVar).append(")");
                } else {
                    sb.append(_var).append(".select(\"").append(expression.toString()).append("\"");
                }
                if (as != null && !(expression instanceof SubSelect)) {
                    sb.append(", \"").append(as).append("\"");
                }
                appendLine(");");
            }
        };
        for (SelectItem selectItem : selectItems) {
            selectItem.accept(itemVisitor);
        }
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        List<SelectBody> selects = setOperationList.getSelects();
        List<String> _vars = new ArrayList<>();
        if (selects != null && !selects.isEmpty()) {
            for (SelectBody select : selects) {
                DbSelectVisitor selectVisitor = new DbSelectVisitor();
                selectVisitor.withAs = withAs;
                select.accept(selectVisitor);
                sb.append(selectVisitor.sb);
                _vars.add(selectVisitor.var);
            }
            var = _vars.get(0);
        }
        List<SetOperation> operations = setOperationList.getOperations();
        if (operations != null && !operations.isEmpty()) {
            int index = 1;
            sb.append("\r\n");
            for (SetOperation operation : operations) {
                if (operation instanceof UnionOp) {
                    boolean all = ((UnionOp) operation).isAll();
                    sb.append(var).append(all ? ".unionAll(" : ".union(").append(_vars.get(index++)).append(");\r\n");
                }
            }
        }
        orderBy(sb, var, setOperationList.getOrderByElements());
        limit(sb, var, setOperationList.getLimit());
    }

    @Override
    public void visit(WithItem withItem) {
    }

    private void appendLine() {
        sb.append("\r\n");
    }

    private void appendLine(String s) {
        sb.append(s).append("\r\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
