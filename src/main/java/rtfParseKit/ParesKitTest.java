package rtfParseKit;

import com.rtfparserkit.parser.RtfStreamSource;
import com.rtfparserkit.parser.standard.StandardRtfParser;
import rtfParseKit.MyRtfParseKit.TreeChanger;
import rtfParseKit.elements.MyGroup;
import rtfParseKit.elements.RootGroup;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParesKitTest {
    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args) {
        final File INPUT_FILE = new File("src/main/resources/Запрос на поставку.rtf");
        final File OUTPUT_FILE = new File("src/main/resources/test1.rtf");


        InputStream is;
        OutputStream out;
        try {

//            RtfStringSource stringSource = new RtfStringSource(readFile("src/main/resources/Запрос на поставку.rtf", Charset.forName("windows-1251")));
//            Field f = stringSource.getClass().getDeclaredField("data"); //NoSuchFieldException
//            f.setAccessible(true);
//            String data = (String) f.get(stringSource);
            //        System.out.println("data = " + data);

            //System.out.println("stringSource = " + stringSource);
            is = new FileInputStream(INPUT_FILE.getPath());
            out = new FileOutputStream(OUTPUT_FILE.getPath());

            StandardRtfParser parser = new StandardRtfParser();

            RtfStreamSource rtfStreamSource = new RtfStreamSource(is);

            Listener listener = new Listener(out);
            MyListener myListener = new MyListener(out);

            parser.parse(rtfStreamSource, listener);

            RootGroup rootGroup = listener.getRootGroup();

            TreeChanger treeChanger = new TreeChanger(rootGroup);
            MyGroup temp = TreeChanger.getGroupWithText(rootGroup, "s_002.ssSAPnumber");

            TreeChanger.changeOneStringValue(temp, "\\\\s_002.ssSAPnumber\\\\", "SUPERTEST");
            System.out.println(temp.getText());
            listener.writeDocument();


        } catch (IOException e) {
            e.printStackTrace();
        }
//        IRtfSource source = new RtfStreamSource(is);
//        IRtfParser parser = new StandardRtfParser();

//        char q = 'q';
//        System.out.println((int)('\''));


    }

}
