package cn.veasion.db.parser;

import cn.veasion.db.DbException;
import cn.veasion.db.utils.FieldUtils;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

import java.io.StringReader;

/**
 * SQLParseUtils
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class SQLParseUtils {

    /**
     * SQL转db代码
     */
    public static String parseSQLConvert(String sql) {
        try {
            JSqlParser parser = new CCJSqlParserManager();
            Statement statement = parser.parse(new StringReader(sql));
            DbStatementVisitor dbStatementVisitor = new DbStatementVisitor();
            statement.accept(dbStatementVisitor);
            return dbStatementVisitor.toString();
        } catch (Exception e) {
            throw new DbException("sql解析异常", e);
        } catch (NoClassDefFoundError t) {
            throw new DbException("jsqlparser maven 作用域为 provided 请手动加入该 jar 依赖", t);
        } finally {
            DbFromItemVisitor.NAME_INDEX.remove();
        }
    }

    static String getTableClass(String table) {
        return FieldUtils.firstCase(FieldUtils.lineToHump(getSimpleTable(table)), false) + "PO.class";
    }

    static String getByTable(Table table) {
        return getTableClass(table.getName()).replace(".class", "");
    }

    static String getVarByTable(Table table) {
        Alias alias = table.getAlias();
        if (alias != null && alias.getName() != null) {
            return alias.getName();
        }
        return getSimpleTable(table.getName());
    }

    static String getSimpleTable(String table) {
        table = sqlTrim(table);
        if (table.length() > 1 && table.charAt(1) == '_') {
            table = table.substring(2);
        }
        return table;
    }

    static String getColumnField(String var, Column column) {
        String f = SQLParseUtils.columnToField(column.getColumnName());
        if (column.getTable() != null && !"".equals(column.getTable().toString())) {
            String _var = SQLParseUtils.getVarByTable(column.getTable());
            if (!_var.equals(var)) {
                if (column.getTable() != null) {
                    f = column.getTable().toString() + "." + f;
                }
            }
        }
        return f;
    }

    static String columnToField(String s) {
        return FieldUtils.lineToHump(sqlTrim(s));
    }

    static String sqlTrim(String s) {
        if ((s.startsWith("`") && s.endsWith("`"))
                || (s.startsWith("'") && s.endsWith("'"))
                || (s.startsWith("\"") && s.endsWith("\""))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}
