package rtfParseKit;

import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;
import rtfParseKit.MyRtfParseKit.TreeChanger;
import rtfParseKit.elements.RootGroup;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParesKit {
    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    final static List<String> variables = new ArrayList<>(Arrays.asList
            ("\\\\s_002.ssYppName\\\\", "\\\\s_002.ssnumber\\\\", "\\\\s_002.ssSAPnumber\\\\", "\\\\ s_001. sCustomerPersonHL\\\\", "\\\\s_001. nsPermitNumber\\\\", "\\\\s_001.ddPermitDate\\\\",
                    "\\\\s_001. ssRepNumAggrAgency\\\\", "\\\\s_001. ddDateAggrAgency\\\\", "\\\\s_001.sDocMC\\\\"));

    final static List<String> testVariables = new ArrayList<>(Arrays.asList
            ("test1", "test2", "test3", "test4", "test5", "test6",
                    "test7", "test8", "test9"));

    public static void main(String[] args) throws IOException {
        final File INPUT_FILE = new File("src/main/resources/Запрос на поставку.rtf");
        final File INPUT_FILE2 = new File("src/main/resources/temp/USR_PRJ_KVREP.rtf");
        final File INPUT_FILE3 = new File("src/main/resources/temp/Вкладной лист кассовой книги.rtf");
        final File INPUT_FILE4 = new File("src/main/resources/temp/Платежное поручение.rtf");
        final File OUTPUT_FILE = new File("src/main/resources/test1.rtf");


        InputStream is;
        OutputStream out;

        is = new FileInputStream(INPUT_FILE4.getPath());
        out = new FileOutputStream(OUTPUT_FILE.getPath());

        StandardRtfParser parser = new StandardRtfParser();
        RtfStreamSource rtfStreamSource = new RtfStreamSource(is);
        Listener listener = new Listener(out);
        parser.parse(rtfStreamSource, listener);

        RootGroup rootGroup = listener.getRootGroup();
        TreeChanger treeChanger = new TreeChanger(rootGroup);

//        TreeChanger.changeStringValues(rootGroup, variables, testVariables);
//        TreeChanger.scanCopy();
        listener.writeDocument();


    }

}
