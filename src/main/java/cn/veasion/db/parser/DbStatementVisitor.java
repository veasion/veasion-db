package cn.veasion.db.parser;

import cn.veasion.db.utils.FieldUtils;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * DbStatementVisitor
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class DbStatementVisitor extends StatementVisitorAdapter {

    StringBuilder sb = new StringBuilder();

    @Override
    public void visit(Insert insert) {
        Table table = insert.getTable();
        List<Column> columns = insert.getColumns();
        ItemsList itemsList = insert.getItemsList();
        Select select = insert.getSelect();
        String var = SQLParseUtils.getVarByTable(table);
        if (select != null) {
            DbSelectVisitor selectVisitor = new DbSelectVisitor();
            select.getSelectBody().accept(selectVisitor);
            sb.append("\r\n// 查询字段或别名会自动对应新增字段，如果查询字段跟新增字段不一致请加别名即可");
            sb.append(selectVisitor.sb);
            sb.append("\r\n");
            sb.append("BatchEntityInsert batchInsert = new BatchEntityInsert(").append(selectVisitor.var).append(");");
        } else {
            String entity = SQLParseUtils.getByTable(table);
            sb.append(entity).append(" ").append(var);
            sb.append(" = new ").append(entity).append("();\r\n");
            if (columns != null && !columns.isEmpty()) {
                for (Column column : columns) {
                    String fieldSetter = FieldUtils.firstCase(SQLParseUtils.columnToField(column.getColumnName()), false);
                    sb.append(var).append(".set").append(fieldSetter).append("(null);\r\n");
                }
            }
            sb.append("\r\n");
            if (itemsList instanceof MultiExpressionList) {
                sb.append("List<").append(entity);
                sb.append("> list = new ArrayList<>();\r\n");
                sb.append("list.add(").append(var).append(");\r\n");
                sb.append("BatchEntityInsert batchInsert = new BatchEntityInsert(list);");
            } else {
                sb.append("EntityInsert insert = new EntityInsert(").append(var).append(");");
            }
        }
    }

    @Override
    public void visit(Delete delete) {
        if (delete.getJoins() != null) {
            sb.append("// 删除暂不支持join\r\n");
        }
        sb.append("Delete delete = new Delete();\r\n");
        Expression where = delete.getWhere();
        if (where != null) {
            DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
            expressionVisitor.var = "delete";
            where.accept(expressionVisitor);
            sb.append(expressionVisitor.sb);
        }
        sb.append("\r\ndelete.setEntityClass(");
        sb.append(SQLParseUtils.getTableClass(delete.getTable().getName())).append(")\r\n");
    }

    @Override
    public void visit(Update update) {
        List<Table> tables = update.getTables();
        List<String> varList = new ArrayList<>();
        for (Table table : tables) {
            String var = SQLParseUtils.getVarByTable(table);
            varList.add(var);
            sb.append("EU ").append(var);
            sb.append(" = new EU(").append(SQLParseUtils.getTableClass(table.getName()));
            Alias alias = table.getAlias();
            if (alias != null && alias.getName() != null) {
                sb.append(", \"").append(SQLParseUtils.sqlTrim(alias.getName())).append("\"");
            }
            sb.append(");\r\n");
        }
        sb.append("\r\n");
        String var = varList.get(0);
        List<Column> columns = update.getColumns();
        List<Expression> expressions = update.getExpressions();
        if (columns != null && !columns.isEmpty()) {
            DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                String field = SQLParseUtils.getColumnField(var, column);
                Expression expression = expressions.get(i);
                if (expression instanceof Function || expression instanceof Column) {
                    sb.append(var).append(".updateExpression(\"").append(field).append("\", ");
                    sb.append("\"").append(expression.toString()).append("\"");
                } else {
                    sb.append(var).append(".update(\"").append(field).append("\", ");
                    expressionVisitor.var = var;
                    expressionVisitor.sb = sb;
                    expression.accept(expressionVisitor);
                }
                sb.append(");\r\n");
            }
        }
        Expression where = update.getWhere();
        if (where != null) {
            DbExpressionVisitor expressionVisitor = new DbExpressionVisitor();
            expressionVisitor.var = var;
            where.accept(expressionVisitor);
            sb.append(expressionVisitor.sb);
        }
        sb.append("\r\n");
        for (int i = 1; i < tables.size(); i++) {
            sb.append(var).append(".join(").append(varList.get(i)).append(");\r\n");
        }
    }

    @Override
    public void visit(Select select) {
        DbSelectVisitor selectVisitor = new DbSelectVisitor();
        select.getSelectBody().accept(selectVisitor);
        sb.append(selectVisitor.toString());
    }

    @Override
    public String toString() {
        return sb.length() == 0 ? "暂不支持该类型转换" : sb.toString();
    }

}
