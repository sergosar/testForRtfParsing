package rtf;

import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;
import rtf.elements.RootGroup;
import rtf.listener.Listener;
import rtf.print.PrintContext;
import rtf.util.TreeChanger;
import rtf.util.Utils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ������������ ������
 */
public class RtfEngine {
    private static final Logger logger = Logger.getLogger(RtfEngine.class.getCanonicalName());

    public static final String VNIIEFNS = "ru:vniief:rtf:1.0";


    public void render(String path, String templatePath, PrintContext context) throws Exception {
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

        //       Map<String, List<Map<String, Object>>> valuesMap = getRSValues(context);
        Map<String, List<Map<String, Object>>> valuesMap = new HashMap<>();

        String scanPar = treeChanger.getScanPar();
        while (scanPar != null) {
//            int quantity = valuesMap.get(scanPar).size();
            int quantity = 1;
            if (treeChanger.isOnlyTableRowsBetweenScans(rootGroup, scanPar)) {
                logger.log(Level.INFO, "while (scanPar !  = " + scanPar);
                treeChanger.addAndFillRowsFromPrintContext(rootGroup, scanPar, quantity - 1, valuesMap);
                Utils.fullRefreshIndexes(rootGroup);
                treeChanger.deleteGroupsWithScan(rootGroup, scanPar);
                Utils.fullRefreshIndexes(rootGroup);
            } else {
                treeChanger.changeStringValuesFromMapListBetweenScanEnd(rootGroup, scanPar, valuesMap);
                Utils.fullRefreshIndexes(rootGroup);
            }
            scanPar = treeChanger.getScanPar();
        }
        treeChanger.changeStringValuesFromMapList(rootGroup, valuesMap);
        Utils.fullRefreshIndexes(rootGroup);
       // treeChanger.changeStringValuesFromContext(rootGroup, context);

        listener.writeDocument();
        try {
            is.close();
            out.close();
        } catch (IOException e) {
            logger.log(Level.OFF, "IOException " + e.getMessage() + " " + e.getCause());
            for (StackTraceElement el : e.getStackTrace()) {
                logger.log(Level.OFF, "el = " + el.toString());
            }
        }

    }


    public static void main(String[] args) {
        String path = "src/main/resources/test.rtf";
        String templatePath = "src/main/resources/temp/test2.rtf";
        String templatePath2 = "src/main/resources/temp/USR_PRJ_KVREP.rtf";
        String templatePath3 = "src/main/resources/temp/�������� ���� �������� �����.rtf";
        String templatePath4 = "src/main/resources/������������/�������� ��������� � ������������� T���_12.rtf";
        String templatePath5 = "src/main/resources/os6_all.rtf";


        try {
            new RtfEngine().render(path, templatePath5, new PrintContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    private static Map<String, List<Map<String, Object>>> getRSValues(PrintContext context) throws Exception {
//        Map<String, List<Map<String, Object>>> values = new HashMap<>();
//        for (var entry : context.getCursors().entrySet()) {
//            values.put(entry.getKey(), new ArrayList<>());
//            String entryKey = entry.getKey();
//            ResultSet rs = entry.getValue();
//            while (rs.next()) {
//                Map<String, Object> map = Utils.toMap(rs, entry.getKey());
//                values.get(entryKey).add(map);
//            }
//        }
//        return values;
}



