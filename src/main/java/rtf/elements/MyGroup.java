package rtf.elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Мирошниченко Сергей
 */
public class MyGroup implements Writeable {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    final public static Charset utfCharset = StandardCharsets.UTF_8;

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final List<Writeable> innerGroups = new ArrayList<>();
    private MyGroup parentGroup = null;
    private int groupIndex;
    private boolean hasSemicolon;

    public void setHasSemicolon(boolean hasSemicolon) {
        this.hasSemicolon = hasSemicolon;
    }
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

    public Writeable getLastWriteable() {
        return innerGroups.get(innerGroups.size() - 1);
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

    public MyGroup() {
    }

    public MyGroup(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    @Override
    public byte[] bytesForWriting() {
//        System.out.println("groupIndex:  " + groupIndex);
        try {
            if (baos.size() > 0) {
                baos.reset();
            }
            baos.write("{".getBytes(utfCharset));
            for (Writeable writeable : innerGroups) {
                baos.write(writeable.bytesForWriting());
            }
//            if(hasSemicolon) {
//                baos.write(";".getBytes(utfCharset));
//            }
            baos.write("}".getBytes(utfCharset));
            if(hasSemicolon) {
                baos.write(";".getBytes(utfCharset));
            }
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