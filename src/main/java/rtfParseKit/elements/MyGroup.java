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

    //  List<MyCommand> commands = new ArrayList<>();

    String innerString = null;

    MyGroup parentGroup = null;

    public void addCommand(MyCommand myCommand) {
        innerGroups.add(myCommand);
    }

    public void addGroup(MyGroup myGroup) {
        innerGroups.add(myGroup);
    }

    public void addCommandParams(CommandParams commandParams) {
        innerGroups.add(commandParams);
    }

    public boolean isLastCommand(){
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

    @Override
    public byte[] bytesForWriting() {

        try {
            baos.write("{".getBytes(utfCharset));
            for (Writeable writeable : innerGroups) {
                baos.write(writeable.bytesForWriting());
            }
            if (innerString != null) {
                if (innerString.contains("\\")) {
                    innerString = innerString.replace("\\", "\\\\");
                }

                Writeable lastW = innerGroups.get(innerGroups.size() - 1);
                if ((lastW instanceof MyCommand) && (
                        ((MyCommand) lastW).command.equals(Command.leveltemplateid) || lastW.equals(Command.levelnumbers))) {
                    baos.write(HexUtil.getHex2(innerString).getBytes(utfCharset));
                } else {
                    baos.write(HexUtil.getHex(innerString).getBytes(utfCharset));
                }

            }
            baos.write("}".getBytes(utfCharset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();

    }


    @Override
    public String getText() {
        return null;
    }
}
