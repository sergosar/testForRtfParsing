package openRtf;

import com.lowagie.text.Document;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;

import java.io.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

//        final File OUTPUT_FILE = new File("target/CreateSimpleRTFDocumentTest.rtf");
        final File OUTPUT_FILE = new File("src/main/resources/1234.rtf");
        InputStream is = new FileInputStream("src/main/resources/Запрос на поставку.rtf");
        OutputStream out = new FileOutputStream("src/main/resources/1234.rtf");
        RtfDocument rtfDocument = new RtfDocument();
      //  rtfDocument.
        Document output = new Document();
        RtfParser rtfParser = new RtfParser(new Document());



        try {
            rtfParser.importRtfDocument(is, rtfDocument);
    //        rtfDocument
            rtfDocument.writeDocument(out);
            //output.

            System.out.println("rtfParser = " + rtfParser.getCurrentDestination());


  //          rtfParser.convertRtfDocument(is, output);
//            rtfParser.
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
