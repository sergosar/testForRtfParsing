package rtf.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    public Map<String, Object> toMap(ResultSet rs, String name) throws SQLException {
        Map<String, Object> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            result.put(String.format("\\%s.%s\\", name, metaData.getColumnLabel(i)), rs.getObject(i));
        }
        return result;
    }
}
