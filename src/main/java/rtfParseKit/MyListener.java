package rtfParseKit;

import com.rtfparserkit.parser.IRtfListener;
import com.rtfparserkit.rtf.Command;
import org.apache.commons.codec.binary.Hex;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyListener implements IRtfListener {

    byte[] temp;

    int commandCount = 0;
    int stringCount = 0;

    int groupDepth=1;
    OutputStream os;

    final static Charset windowsCharset = Charset.forName("windows-1251");
    final static Charset utf16Charset = StandardCharsets.UTF_16;
    final Charset utfCharset = StandardCharsets.UTF_8;
    Charset lastCharset = null;
    String lastCharsetStr = null;

    Command previousCommand = null;

    final List<String> varibbles = new ArrayList<>(Arrays.asList
            ("s_002.ssYppName","s_002.ssSAPnumber","s_002.ssSAPnumber","s_002.ssnumber","s_001. sCustomerPersonHL","s_001. sCustomerPersonHL",
                    "s_001. ssRepNumAggrAgency","s_001. ddDateAggrAgency","s_001.sDocMC"));

    public MyListener(OutputStream os) {
        this.os = os;
    }

    @Override
    public void processDocumentStart() {
        System.out.println("processDocumentStart");

    }

    @Override
    public void processDocumentEnd() {
        System.out.println("processDocumentEnd");
    }

    @Override
    public void processGroupStart() {
        var depth = "  ".repeat(groupDepth);
        System.out.println(groupDepth +depth + "{");

        try {
            os.write("{".getBytes(utfCharset));
        } catch (IOException e) {
            e.printStackTrace();
        }
        groupDepth++;
    }

    @Override
    public void processGroupEnd() {
        groupDepth--;
        var depth = "  ".repeat(groupDepth);
        System.out.println(groupDepth + depth + "}");
        try {
            os.write("}".getBytes(utfCharset));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processCharacterBytes(byte[] bytes) {
        System.out.println("processCharacterBytes" + Arrays.toString(bytes));
    }

    @Override
    public void processBinaryBytes(byte[] bytes) {
        System.out.println("processBinaryBytes!!!!!!!!!!!!!!!!!!!!!!!!!!! " + Arrays.toString(bytes));

    }

    @Override
    public void processString(String s) {
        String hexStr;
        //       if (previousCommand.equals(Command.leveltemplateid)) {
        System.out.println("processString N " + stringCount + ": " + s);


  //      if
        if (s.contains("\\")) {
            s = s.replace("\\", "\\\\");
            System.out.println("s = " + s);
        }

        if (previousCommand.equals(Command.leveltemplateid) || previousCommand.equals(Command.levelnumbers)) {
            hexStr = getHex2(s);
        } else hexStr = getHex(s);

        try {
            temp = hexStr.getBytes(utfCharset);
            os.write(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stringCount++;
    }

    @Override
    public void processCommand(Command command, int i, boolean b, boolean b1) {
        previousCommand = command;

        //     if (command.equals(Command.leveltemplateid))
        System.out.println("command N " + commandCount + ": " + command + ",  int i = " + i + ",  boolean b = " + b + ", boolean b1 = " + b1);

//        if(command.equals(Command.fcharset)) {
//            System.out.println("!!!!!!!!!!!!!!!!" + command + ",  int i = " + i );
//            lastCharsetStr = String.valueOf(i);
//         //   lastCharset = Charset.forName(MyFontCharset.getCharset(i));
//        }

        StringBuilder stringBuilder = new StringBuilder();

        if (b1) {
            stringBuilder.append("\\*");
        }
        stringBuilder.append("\\");
        stringBuilder.append(command.getCommandName());

        if (b) {
            stringBuilder.append(i);
        }

        stringBuilder.append(" ");

        try {
            os.write(stringBuilder.toString().getBytes(utfCharset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        commandCount++;
    }


//    public  String getHex(String s) {
//
//        StringBuilder result = new StringBuilder();
//        String temp = null;
//
//        for (int i = 0; i < s.length(); i++) {
//            if (s.codePointAt(i) < 128) {
//                result.append(s.charAt(i));
//            } else {
//                temp = String.valueOf(s.charAt(i));
//                try {
//                    result.append(HexFormat.of().withPrefix("\\'").formatHex(temp.getBytes(windowsCharset)));
//                } catch (UnsupportedEncodingException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//
//        }

//
//        return result.toString();
//    }

    public static String getHex(String s) {

        StringBuilder result = new StringBuilder();
        String temp;

        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) < 128) {
                result.append(s.charAt(i));
            } else {
                temp = String.valueOf(s.charAt(i));

                result.append("\\").append("'").append(Hex.encodeHexString(temp.getBytes(windowsCharset)));
            }
        }
        return result.toString();
    }


    public static String getHex2(String s) {

        StringBuilder result = new StringBuilder();
        String temp;

        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) < 10) {
                temp = String.valueOf(s.charAt(i));
                result.append("\\").append("'").append(Hex.encodeHexString(temp.getBytes(windowsCharset)));
            } else if (s.codePointAt(i) < 128) {
                result.append(s.charAt(i));
            } else {
                if (s.codePointAt(i) > 32768) {
                    temp = "\\u" + (s.codePointAt(i) - 65536) + " ?";
                } else temp = "\\u" + s.codePointAt(i) + " ?";

                result.append(temp);
            }
        }


        return result.toString();
    }


    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}

