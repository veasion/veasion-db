package cn.veasion.db.parser;

/**
 * SQLLogParseUtils
 *
 * @author luozhuowei
 * @date 2022/11/18
 */
public class SQLLogParseUtils {

    /**
     * SQL日志拼接成完整SQL（填充参数）
     *
     * @param log 日志
     * @return sql
     */
    public static String log2sql(String log) {
        int sqlStartIdx = log.indexOf("Preparing:");
        if (sqlStartIdx == -1) {
            return null;
        }
        sqlStartIdx += "Preparing:".length();
        int sqlEndIdx = log.indexOf("\n", sqlStartIdx);
        if (sqlEndIdx == -1) {
            return log.substring(sqlStartIdx).replace("\r", "").trim();
        }
        String sql = log.substring(sqlStartIdx, sqlEndIdx).trim();
        int paramStartIdx = log.indexOf("Parameters:", sqlEndIdx);
        if (paramStartIdx == -1) {
            return sql;
        }
        paramStartIdx += "Parameters:".length();
        String param;
        int paramEndIdx = log.indexOf("\n", paramStartIdx);
        if (paramEndIdx == -1) {
            param = log.substring(paramStartIdx);
        } else {
            param = log.substring(paramStartIdx, paramEndIdx).replace("\r", "").trim();
        }
        int qIdx = -1, qCount = 0;
        while ((qIdx = sql.indexOf("?", qIdx + 1)) > -1) {
            ++qCount;
        }
        param = param.replace("null, ", "NULL(String), ");
        if (param.endsWith(", null")) {
            param = param.substring(0, param.length() - ", null".length()) + ", NULL(String)";
        }
        String[] params = param.split("\\(\\w+\\), ", qCount);
        if (params.length > 0) {
            String last = params[params.length - 1];
            params[params.length - 1] = last.substring(0, last.lastIndexOf("("));
        }
        for (String p : params) {
            if ("NULL".equals(p)) {
                p = "null";
            } else if (p.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
                p = "'" + p.replace("T", " 00:") + "'";
            } else if (!p.matches("\\d+")) {
                p = "'" + p.replace("'", "\\'").replace("\"", "\\\"") + "'";
            }
            qIdx = sql.indexOf("?");
            sql = sql.substring(0, qIdx) + p + sql.substring(qIdx + 1);
        }
        return sql;
    }

}
