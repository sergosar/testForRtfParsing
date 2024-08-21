package rtfParseKit.elements;

import com.rtfparserkit.rtf.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyGroup implements Writeable {
    final static Charset utfCharset = StandardCharsets.UTF_8;

    StringBuilder stringForWrite = new StringBuilder();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    List<Writeable> innerGroups = new ArrayList<>(); // нужно ли сразу инициализировать?

    String innerString = null;

    MyGroup parentGroup = null;

    private int groupIndex;

    public void addCommand(MyCommand myCommand) {
        innerGroups.add(myCommand);
    }

    public void addGroup(MyGroup myGroup) {
        innerGroups.add(myGroup);
    }

    public void addCommandParams(CommandParams commandParams) {
        innerGroups.add(commandParams);
    }

    public void addString(MyString myString) {
        innerGroups.add(myString);
    }

    public boolean isLastCommand() {
        return innerGroups.get(innerGroups.size() - 1) instanceof MyCommand;
    }

    public MyCommand getLastCommand() {
        return (MyCommand) innerGroups.get(innerGroups.size() - 1);
    }

    public void setStringForWrite(String innerString) {
        this.innerString = innerString;
    }

    public MyGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(MyGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public MyGroup() {
    }

    public MyGroup(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    @Override
    public byte[] bytesForWriting() {


        System.out.println("groupIndex:  " + groupIndex);
        try {
            if(baos.size()>0) {
                baos.reset();
            }
            baos.write("{".getBytes(utfCharset));
            for (Writeable writeable : innerGroups) {
                baos.write(writeable.bytesForWriting());
            }
//            if (innerString != null) {
//                if (innerString.contains("\\")) {
//                    innerString = innerString.replace("\\", "\\\\");
//                }
//                System.out.println("innerString != null");
//                Writeable lastW = innerGroups.get(innerGroups.size() - 1);
//                if ((lastW instanceof MyCommand) && (
//                        ((MyCommand) lastW).command.equals(Command.leveltemplateid) || lastW.equals(Command.levelnumbers))) {
//                    baos.write(HexUtil.getHex2(innerString).getBytes(utfCharset));
//                } else {
//                    baos.write(HexUtil.getHex(innerString).getBytes(utfCharset));
//                }
//
//            }
            baos.write("}".getBytes(utfCharset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();

    }


    public List<Writeable> getInnerGroups() {
        return innerGroups;
    }

    @Override
    public String getText() {
        return null;
    }
}
