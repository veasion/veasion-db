package cn.veasion.db.parser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * DbExpressionVisitor
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public class DbExpressionVisitor implements ExpressionVisitor {

    String var;
    boolean onFilter;
    boolean havingFilter;
    String joinUseMainVar, joinVar;
    StringBuilder master = new StringBuilder();
    StringBuilder sb = new StringBuilder();

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {
        sb.append(bitwiseRightShift.toString());
    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {
        sb.append(bitwiseLeftShift.toString());
    }

    @Override
    public void visit(NullValue nullValue) {
        sb.append(nullValue.toString());
    }

    @Override
    public void visit(Function function) {
        sb.append("\"").append(function.toString()).append("\"");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        sb.append(signedExpression.toString());
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        sb.append(doubleValue.toString());
    }

    @Override
    public void visit(LongValue longValue) {
        sb.append(longValue.toString());
    }

    @Override
    public void visit(HexValue hexValue) {
        sb.append("\"").append(hexValue.toString()).append("\"");
    }

    @Override
    public void visit(DateValue dateValue) {
        sb.append("new Date()");
    }

    @Override
    public void visit(TimeValue timeValue) {
        sb.append("new Date()");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        sb.append("new Date()");
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        appendVarFunFilterByType("leftBracket()", true);
        parenthesis.getExpression().accept(this);
        appendVarFunFilterByType("rightBracket()", true);
    }

    @Override
    public void visit(StringValue stringValue) {
        sb.append("\"").append(stringValue.getValue()).append("\"");
    }

    @Override
    public void visit(Addition addition) {
        addition.getLeftExpression().accept(this);
        sb.append(" + ");
        addition.getRightExpression().accept(this);
    }

    @Override
    public void visit(Division division) {
        division.getLeftExpression().accept(this);
        sb.append(" / ");
        division.getRightExpression().accept(this);
    }

    @Override
    public void visit(Multiplication multiplication) {
        multiplication.getLeftExpression().accept(this);
        sb.append(" * ");
        multiplication.getRightExpression().accept(this);
    }

    @Override
    public void visit(Subtraction subtraction) {
        subtraction.getLeftExpression().accept(this);
        sb.append(" - ");
        subtraction.getRightExpression().accept(this);
    }

    @Override
    public void visit(AndExpression andExpression) {
        if (andExpression.getLeftExpression() != null) {
            andExpression.getLeftExpression().accept(this);
        }
        if (andExpression.getRightExpression() != null) {
            andExpression.getRightExpression().accept(this);
        }
    }

    @Override
    public void visit(OrExpression orExpression) {
        if (orExpression.getLeftExpression() != null) {
            orExpression.getLeftExpression().accept(this);
        }
        appendVarFunFilterByType("or()", true);
        if (orExpression.getRightExpression() != null) {
            orExpression.getRightExpression().accept(this);
        }
    }

    @Override
    public void visit(Between between) {
        if (havingFilter) {
            appendVarFun("having(Filter.between(", false);
        } else if (onFilter) {
            appendVarFun("on(Filter.between(", false);
        } else {
            appendVarFun("between(", false);
        }
        between.getLeftExpression().accept(this);
        sb.append(", ");
        between.getBetweenExpressionStart().accept(this);
        sb.append(", ");
        between.getBetweenExpressionEnd().accept(this);
        if (havingFilter || onFilter) {
            sb.append(")");
        }
        sb.append(")");
        if (!onFilter) {
            sb.append(";\r\n");
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();
        if (onFilter && leftExpression instanceof Column && rightExpression instanceof Column) {
            sb.append(".on(\"").append(SQLParseUtils.getColumnField(joinUseMainVar, (Column) leftExpression));
            sb.append("\", \"").append(SQLParseUtils.getColumnField(joinVar, (Column) rightExpression));
            sb.append("\")");
        } else {
            handleComparisonOperator(equalsTo, "eq");
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        handleComparisonOperator(greaterThan, "gt");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        handleComparisonOperator(greaterThanEquals, "gte");
    }

    @Override
    public void visit(InExpression inExpression) {
        Expression leftExpression = inExpression.getLeftExpression();
        ItemsList rightItemsList = inExpression.getRightItemsList();
        boolean subQuery = rightItemsList instanceof SubSelect;
        boolean leftColumn = leftExpression instanceof Column;
        if (leftColumn) {
            String column = SQLParseUtils.getColumnField(var, (Column) leftExpression);
            column = "\"" + column + "\"";
            String s = (subQuery ? "subQuery(" : (inExpression.isNot() ? "notIn(" : "in(")) + column;
            if (havingFilter) {
                appendVarFun("having(Filter." + s, false);
            } else if (onFilter) {
                appendVarFun("on(Filter." + s, false);
            } else {
                if (subQuery) {
                    s = "filterSubQuery(" + column;
                }
                appendVarFun(s, false);
            }
            if (subQuery) {
                sb.append(", Operator.").append(inExpression.isNot() ? "NOT_IN" : "IN");
            }
            sb.append(", ");
            if (rightItemsList instanceof ExpressionList) {
                String list = rightItemsList.toString();
                if (list.startsWith("(") && list.endsWith(")")) {
                    sb.append("Arrays.asList").append(list);
                } else {
                    sb.append("new Object[]{}");
                }
            } else if (subQuery) {
                SubSelect subSelect = (SubSelect) rightItemsList;
                String _var = SubSelectVisitor.visit(master, subSelect);
                sb.append("SubQueryParam.build(").append(_var).append(")");
            }
            if (havingFilter || onFilter) {
                sb.append(")");
            }
            sb.append(")");
            if (!onFilter) {
                sb.append(";\r\n");
            }
        }
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        leftRight(isNullExpression.getLeftExpression(), isNullExpression.isNot() ? "isNotNull" : "isNull", null);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        if (!likeExpression.isNot()) {
            Expression leftExpression = likeExpression.getLeftExpression();
            Expression rightExpression = likeExpression.getRightExpression();
            String opt = "like";
            if (rightExpression instanceof StringValue) {
                String value = ((StringValue) rightExpression).getValue().trim();
                if (value.startsWith("%") && value.endsWith("%")) {
                    opt = "like";
                    value = value.substring(1, value.length() - 1);
                } else if (value.startsWith("%")) {
                    opt = "likeLeft";
                    value = value.substring(1);
                } else if (value.endsWith("%")) {
                    opt = "likeRight";
                    value = value.substring(0, value.length() - 1);
                }
                ((StringValue) rightExpression).setValue(value);
            }
            leftRight(leftExpression, opt, rightExpression);
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        handleComparisonOperator(minorThan, "lt");
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        handleComparisonOperator(minorThanEquals, "lte");
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        handleComparisonOperator(notEqualsTo, "neq");
    }

    @Override
    public void visit(Column column) {
        sb.append("\"").append(SQLParseUtils.getColumnField(var, column)).append("\"");
    }

    @Override
    public void visit(SubSelect subSelect) {
        String _var = SubSelectVisitor.visit(master, subSelect);
        sb.append("SubQueryParam.build(").append(_var).append(")");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        if (!havingFilter && !onFilter) {
            appendVarFun("selectExpression(\"" + caseExpression.toString() + "\", null);", true);
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        if (!havingFilter && !onFilter) {
            appendVarFun("selectExpression(\"" + whenClause.toString() + "\", null);", true);
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if ((onFilter || havingFilter) && existsExpression.getRightExpression() instanceof SubSelect) {
            appendVarFunFilterByType("subQuery(null, Operator." + (existsExpression.isNot() ? "NOT_" : "") + "EXISTS, ", false);
        } else {
            if (existsExpression.isNot()) {
                appendVarFun("notExists(", false);
            } else {
                appendVarFun("exists(", false);
            }
            existsExpression.getRightExpression().accept(this);
        }
        sb.append(")");
        if (!onFilter) {
            sb.append(";\r\n");
        }
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
    }

    @Override
    public void visit(Concat concat) {
        sb.append(concat.toString());
    }

    @Override
    public void visit(Matches matches) {
        sb.append(matches.toString());
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        sb.append(bitwiseAnd.toString());
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        sb.append(bitwiseOr.toString());
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        sb.append(bitwiseXor.toString());
    }

    @Override
    public void visit(CastExpression castExpression) {
    }

    @Override
    public void visit(Modulo modulo) {
        modulo.getLeftExpression().accept(this);
        sb.append(" % ");
        modulo.getRightExpression().accept(this);
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {
    }

    @Override
    public void visit(ExtractExpression extractExpression) {
    }

    @Override
    public void visit(IntervalExpression intervalExpression) {
        sb.append(intervalExpression.toString());
    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {
        sb.append(regExpMatchOperator.toString());
    }

    @Override
    public void visit(JsonExpression jsonExpression) {
    }

    @Override
    public void visit(JsonOperator jsonOperator) {
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        sb.append(regExpMySQLOperator.toString());
    }

    @Override
    public void visit(UserVariable userVariable) {
        sb.append(userVariable.toString());
    }

    @Override
    public void visit(NumericBind numericBind) {
    }

    @Override
    public void visit(KeepExpression keepExpression) {
    }

    @Override
    public void visit(MySQLGroupConcat mySQLGroupConcat) {
        sb.append(mySQLGroupConcat.toString());
    }

    @Override
    public void visit(ValueListExpression valueListExpression) {
        sb.append("new Object[]{}");
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
    }

    @Override
    public void visit(OracleHint oracleHint) {
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        sb.append("\"").append(timeKeyExpression.toString()).append("\"");
    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {
    }

    @Override
    public void visit(NotExpression notExpression) {
        sb.append("not");
        Expression expression = notExpression.getExpression();
        if (expression != null) {
            expression.accept(this);
        }
    }

    private void appendVarFunFilterByType(String s, boolean line) {
        if (havingFilter) {
            appendVarFun("having(Filter." + s + ");", line);
        } else if (onFilter) {
            appendVarFun("on(Filter." + s + ");", line);
        } else {
            appendVarFun(s + ";", line);
        }
    }

    private void appendVarFun(String s, boolean line) {
        if (s.startsWith("on(")) {
            sb.append(".").append(s);
        } else {
            sb.append(var).append(".").append(s);
            if (line) {
                sb.append("\r\n");
            }
        }
    }

    private void handleComparisonOperator(ComparisonOperator operator, String opt) {
        Expression leftExpression = operator.getLeftExpression();
        Expression rightExpression = operator.getRightExpression();

        if (leftExpression instanceof Function) {
            if (havingFilter) {
                appendVarFun("having(", false);
            } else if (onFilter) {
                appendVarFun("on(", false);
            } else {
                appendVarFun("addFilter(", false);
            }
            sb.append("Filter.sqlFilter(fieldHandler -> \"").append(operator.toString()).append("\")");
            sb.append(")");
            if (!onFilter) {
                sb.append(";\r\n");
            }
        } else {
            leftRight(leftExpression, opt, rightExpression);
        }
    }

    private void leftRight(Expression leftExpression, String opt, Expression rightExpression) {
        boolean leftColumn = leftExpression instanceof Column;
        boolean rightColumn = rightExpression instanceof Column;
        boolean expression = leftColumn && (rightColumn || rightExpression instanceof Function);

        if (havingFilter) {
            appendVarFun("having(Filter." + (expression ? "expression(" : ""), false);
        } else if (onFilter) {
            appendVarFun("on(Filter." + (expression ? "expression(" : ""), false);
        } else {
            appendVarFun(expression ? "filterExpression(" : "", false);
        }
        if (!expression) {
            sb.append(opt).append("(");
        }
        leftExpression.accept(this);
        if (expression) {
            sb.append(", Operator.").append(opt.toUpperCase());
        }
        if (rightExpression != null) {
            sb.append(", ");
            rightExpression.accept(this);
        }
        if (havingFilter || onFilter) {
            sb.append(")");
        }
        sb.append(")");
        if (!onFilter) {
            sb.append(";\r\n");
        }
    }

}
