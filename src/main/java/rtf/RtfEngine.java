package rtf;

import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;

import rtf.elements.RootGroup;
import rtf.listener.Listener;
import rtf.util.TreeChanger;

import java.io.*;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Мирошниченко Сергей
 */
public class RtfEngine {
    private static final Logger logger = Logger.getLogger(RtfEngine.class.getCanonicalName());

    public static final String VNIIEFNS = "ru:vniief:rtf:1.0";


    public void render(String path, String templatePath) throws Exception {
        final File INPUT_FILE = new File(templatePath);
        final File OUTPUT_FILE = new File(path);

        InputStream is;
        OutputStream out;

        is = new FileInputStream(INPUT_FILE.getPath());
        out = new FileOutputStream(OUTPUT_FILE.getPath());
        StandardRtfParser parser = new StandardRtfParser();
        RtfStreamSource rtfStreamSource = new RtfStreamSource(is);
        Listener listener = new Listener(out);
        parser.parse(rtfStreamSource, listener);
        RootGroup rootGroup = listener.getRootGroup();
        TreeChanger treeChanger = new TreeChanger(rootGroup);


        logger.log(Level.WARNING, "труляля");


        logger.log(Level.WARNING, "entrySet");
        treeChanger.addOneRow(rootGroup);



//        for (var entry : context.getCursors().entrySet()) {
//            logger.log(Level.WARNING, "keyEntry = " + entry.getKey());
//            ResultSet rs = entry.getValue();
//            rs.next();
//            logger.log(Level.WARNING, "rs.next();" + rs.getMetaData().getTableName(1));
//            int count = rs.getMetaData().getColumnCount();
//            Map<String, Object> values = treeChanger.toMap(rs, entry.getKey());
//            for (Map.Entry<String, Object> entry2 : values.entrySet()) {
//                logger.log(Level.WARNING, "key = " + entry2.getKey() + "value = " + entry2.getValue());
//            }
//            try {
//                treeChanger.changeStringValuesFromMap(rootGroup, values);
//            } catch (Exception e) {
//                logger.log(Level.WARNING, "NullPointerException" + e.getMessage() + e.getCause());
//                for (StackTraceElement el : e.getStackTrace()) {
//                    logger.log(Level.WARNING, "el = " + el.toString());
//                }
//            }
//
//        }
        listener.writeDocument();

    }

    public static void main(String[] args) {
        String path = "src/main/resources/test.rtf";
        String templatePath = "src/main/resources/temp/test2.rtf";
        String templatePath2 = "src/main/resources/temp/USR_PRJ_KVREP.rtf";
        String templatePath3 = "src/main/resources/temp/Вкладной лист кассовой книги.rtf";

        try {
            new RtfEngine().render(path, templatePath3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
