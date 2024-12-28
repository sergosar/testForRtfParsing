package rtf.util;

import rtf.elements.MyGroup;
import rtf.elements.Writeable;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Мирошниченко Сергей
 */
public class Utils {
    private final static SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat localDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final static DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final static Logger logger = Logger.getLogger(Utils.class.getCanonicalName());
    private static int groupIndex = 1;

    public static Map<String, Object> toMap(ResultSet rs, String name) throws SQLException {
        Map<String, Object> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            logger.log(Level.OFF, "toMap key  " + String.format("\\\\%s.%s\\\\", name, metaData.getColumnLabel(i)));
            logger.log(Level.OFF, "toMap value " + rs.getObject(i));
            result.put(String.format("\\\\%s.%s\\\\", name, metaData.getColumnLabel(i)), rs.getObject(i));
        }
        return result;
    }

    public static String toLocalizedString(Object value) {
        String result;
        if (value == null) {
            result = "";
        } else if (value instanceof String) {
            String s = (String) value;
            // В некоторых отчетах используется кавычка для принудительного
            // указания, что это строка. Видимо, это наследие Excel.
            if (s.length() > 1 && s.charAt(0) == '\'' && Character.isDigit(s.charAt(1))) {
                result = s.substring(1);
            } else {
                result = s;
            }
            // Иногда из СУБД строки приходят с CR+LF, но addTextToElement
            // обрабатывает только LF, что приводит к лишним разрывам строк
            // в тексте ячейки. Поэтому заменим CR+LF на LF.
            result = result.replace("\r\n", "\n");
        } else if (value instanceof BigDecimal) {
            // Преобразуя число в строку, Альфа округляет его до 12 знаков после запятой.
            BigDecimal d = (BigDecimal) value;
            result = BigDecimal.ZERO.equals(d) ? "0" : String.format("%.12f", d);
        } else if (value instanceof Date) {
            Date d = (Date) value;
            result = localDateFormat.format(d);
        } else if (value instanceof LocalDate) {
            LocalDate localDate = (LocalDate) value;
            result = localDate.format(localDateFormatter);
        } else if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            result = localDateTime.format(localDateTimeFormatter);
        } else {
            result = value.toString();
        }
        return result;
    }

    private static void refreshGroupsIndexes(MyGroup myGroup) {
        for (Writeable w : myGroup.getInnerGroups()) {
            if (w instanceof MyGroup) {
                ((MyGroup) w).setGroupIndex(groupIndex++);
                refreshGroupsIndexes((MyGroup) w);
            }
        }
    }

    public static void fullRefreshIndexes(MyGroup myGroup) {
        groupIndex = 1;
        refreshGroupsIndexes(myGroup);
    }

    public static void setGroupIndex(int groupIndex) {
        Utils.groupIndex = groupIndex;
    }
}
