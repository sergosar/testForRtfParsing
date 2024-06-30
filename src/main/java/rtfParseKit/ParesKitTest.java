package rtfParseKit;

import com.rtfparserkit.converter.text.StringTextConverter;
import com.rtfparserkit.parser.*;
import com.rtfparserkit.parser.standard.StandardRtfParser;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParesKitTest {
    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args) {
        final File INPUT_FILE = new File("src/main/resources/Запрос на поставку.rtf");
        final File OUTPUT_FILE = new File("src/main/resources/test.rtf");




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


            parser.parse(rtfStreamSource, new MyListener2(out));


        } catch (IOException e) {
            e.printStackTrace();
        }
//        IRtfSource source = new RtfStreamSource(is);
//        IRtfParser parser = new StandardRtfParser();

//        char q = 'q';
//        System.out.println((int)('\''));




    }

}
