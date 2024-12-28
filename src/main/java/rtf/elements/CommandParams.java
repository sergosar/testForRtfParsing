package rtf.elements;

/**
 * @author Мирошниченко Сергей
 */
public class CommandParams implements Writeable {
    private String text;

    public void setText(String text) {
        this.text = text;
    }

    public CommandParams() {
    }

    public CommandParams(String text) {
        this.text = text;
    }

    @Override
    public byte[] bytesForWriting() {
        return HexUtil.getHex2(text).getBytes(MyGroup.utfCharset);
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void setHasSemicolon(boolean hasSemicolon) {

    }

    @Override
    public Writeable getCopy() {
        return new CommandParams(this.text);
    }
}
