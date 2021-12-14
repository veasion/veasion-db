package cn.veasion.db.parser;

import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * SubSelectVisitor
 *
 * @author luozhuowei
 * @date 2021/12/14
 */
public class SubSelectVisitor {

    /**
     * 子查询
     *
     * @return 变量名
     */
    public static String visit(StringBuilder sb, SubSelect subSelect, String as) {
        SelectBody selectBody = subSelect.getSelectBody();
        DbSelectVisitor dbSelectVisitor = new DbSelectVisitor();
        selectBody.accept(dbSelectVisitor);
        String code = dbSelectVisitor.sb.toString();
        if (as != null && !"".equals(as)) {
            int idx = code.indexOf(".select");
            if (idx > -1) {
                int endIdx = code.indexOf(");", idx);
                if (endIdx > -1) {
                    code = code.substring(0, endIdx) + ", \"" + as + "\"" + code.substring(endIdx);
                }
            }
        }
        sb.append(code);
        return dbSelectVisitor.var;
    }

    public static String visit(StringBuilder sb, SubSelect subSelect) {
        return visit(sb, subSelect, null);
    }

}
