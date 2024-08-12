package rtfParseKit.elements;

import com.rtfparserkit.rtf.Command;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MyCommand implements Writeable{

    Command command;

    int i;
    boolean b;
    boolean b1;

    boolean hasSemicolon;

    public void setHasSemicolon(boolean hasSemicolon) {
        this.hasSemicolon = hasSemicolon;
    }

    public MyCommand() {
    }

    public Command getCommand() {
        return command;
    }

    public MyCommand(Command command, int i, boolean b, boolean b1) {
        this.command = command;
        this.i = i;
        this.b = b;
        this.b1 = b1;
    }



    @Override
    public byte[] bytesForWriting() {
        StringBuilder stringBuilder = new StringBuilder();

        if (b1) {
            stringBuilder.append("\\*");
        }
        stringBuilder.append("\\");
        stringBuilder.append(command.getCommandName());

        if (b) {
            stringBuilder.append(i);
        }
        if(hasSemicolon){
            stringBuilder.append(" ;");
        }

        if(!hasSemicolon)stringBuilder.append(" ");
        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);

    }

    @Override
    public String getText() {
        return command.getCommandName();
    }
}
