package rtfParseKit;

import com.rtfparserkit.parser.IRtfListener;
import com.rtfparserkit.rtf.Command;
import org.apache.commons.codec.binary.Hex;
import rtfParseKit.elements.CommandParams;
import rtfParseKit.elements.MyCommand;
import rtfParseKit.elements.MyGroup;
import rtfParseKit.elements.RootGroup;


import java.io.IOException;
import java.io.OutputStream;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyListener2 implements IRtfListener {

    byte[] temp;

    int commandCount = 0;
    int stringCount = 0;

    int groupDepth=1;
    OutputStream os;

    RootGroup rootGroup;
    MyGroup currentGroup;


    final static Charset windowsCharset = Charset.forName("windows-1251");
    final static Charset utf16Charset = StandardCharsets.UTF_16;
    final Charset utfCharset = StandardCharsets.UTF_8;
    Charset lastCharset = null;
    String lastCharsetStr = null;

    Command previousCommand = null;

    final List<String> varibbles = new ArrayList<>(Arrays.asList
            ("s_002.ssYppName","s_002.ssSAPnumber","s_002.ssSAPnumber","s_002.ssnumber","s_001. sCustomerPersonHL","s_001. sCustomerPersonHL",
                    "s_001. ssRepNumAggrAgency","s_001. ddDateAggrAgency","s_001.sDocMC"));

    public MyListener2(OutputStream os) {
        this.os = os;
    }

    @Override
    public void processDocumentStart() {
        rootGroup = RootGroup.getInstance();
        currentGroup= rootGroup;

    }

    @Override
    public void processDocumentEnd() {
        System.out.println("processDocumentEnd");
        try {
            os.write(rootGroup.bytesForWriting());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processGroupStart() {
        var depth = "  ".repeat(groupDepth);
        System.out.println(groupDepth +depth + "{");
        if(!rootGroup.start) {
            rootGroup.start=true;
        } else {
            MyGroup newGroup = new MyGroup();
            newGroup.setParentGroup(currentGroup);
            currentGroup.addGroup(newGroup);
            currentGroup = newGroup;
        }
        groupDepth++;
    }

    @Override
    public void processGroupEnd() {
        groupDepth--;
        var depth = "  ".repeat(groupDepth);
        System.out.println(groupDepth + depth + "}");
        currentGroup=currentGroup.getParentGroup();

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
        String hexStr = null;
        //       if (previousCommand.equals(Command.leveltemplateid)) {
        System.out.println("processString N " + stringCount + ": " + s);
        if(s.equals(";")) {
            currentGroup.getLastCommand().setHasSemicolon(true);
        } else if(currentGroup.isLastCommand() && (currentGroup.getLastCommand().getCommand().equals(Command.leveltemplateid)
        || currentGroup.getLastCommand().getCommand().equals(Command.levelnumbers))){
            currentGroup.addCommandParams(new CommandParams(s));
        } else
            currentGroup.setStringForWrite(s);

        stringCount++;
    }

    @Override
    public void processCommand(Command command, int i, boolean b, boolean b1) {
        previousCommand = command;
        currentGroup.addCommand(new MyCommand(command,i,b,b1));

        commandCount++;
    }



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




}
