package rtf.listener;

import com.rtfparserkit.parser.IRtfListener;
import com.rtfparserkit.rtf.Command;
import rtf.elements.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Мирошниченко Сергей
 */
public class Listener implements IRtfListener {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private int commandCount = 0;
    private int stringCount = 0;
    private int groupDepth2Count = 0;
    private int groupDepth = 1;
    private OutputStream os;
    private int groupIndex = 1;
    private RootGroup rootGroup;
    private MyGroup currentGroup;

    public RootGroup getRootGroup() {
        return rootGroup;
    }

    private Command previousCommand = null;

    public Listener(OutputStream os) {
        this.os = os;
    }

    @Override
    public void processDocumentStart() {
        rootGroup = new RootGroup();
        currentGroup = rootGroup;
    }

    @Override
    public void processDocumentEnd() {
        logger.log(Level.FINE, "processDocumentEnd");
    }

    @Override
    public void processGroupStart() {
        var depth = "  ".repeat(groupDepth);
        logger.log(Level.OFF, groupDepth + depth + "{");
        if (groupDepth == 2) {
            groupDepth2Count++;
            logger.log(Level.OFF, "gruopDepth2Count = " + groupDepth2Count + "groupIndex = " + groupIndex);
        }
        if (!rootGroup.start) {
            rootGroup.start = true;
        } else {
            MyGroup newGroup = new MyGroup(groupIndex++);
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
        logger.log(Level.OFF, groupDepth + " " + depth + "}");
        currentGroup = currentGroup.getParentGroup();
    }

    @Override
    public void processCharacterBytes(byte[] bytes) {
        //парсер никогда не использует в rtf файлах, поэтому нет реализации
    }

    @Override
    public void processBinaryBytes(byte[] bytes) {
        //парсер никогда не использует в rtf файлах, поэтому нет реализации
    }

    @Override
    public void processString(String s) {
        logger.log(Level.INFO, "processString: " + s);
        if (s.equals(";")) {
            currentGroup.getLastWriteable().setHasSemicolon(true);
        } else if (currentGroup.isLastCommand() && (currentGroup.getLastCommand().getCommand().equals(Command.leveltemplateid)
                || currentGroup.getLastCommand().getCommand().equals(Command.levelnumbers))) {
            currentGroup.addCommandParams(new CommandParams(s));
        } else {
            if (s.contains("\\")) {
                s = s.replace("\\", "\\\\");
            }
            currentGroup.addString(new MyString(s));
        }
        stringCount++;
    }

    @Override
    public void processCommand(Command command, int i, boolean b, boolean b1) {
        previousCommand = command;
        currentGroup.addCommand(new MyCommand(command, i, b, b1));
        if (commandCount == 1) {
            currentGroup.addCommand(new MyCommand(Command.ansi, 0, false, false));
            currentGroup.addCommand(new MyCommand(Command.ansicpg, 1251, true, false));
            currentGroup.addCommand(new MyCommand(Command.uc, 1, true, false));
        }
        commandCount++;
    }

    public void writeDocument() {
        try {
            os.write(rootGroup.bytesForWriting());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
