package rtfParseKit.elements;

public interface Writeable {
    byte[] bytesForWriting();

    String getText();
    public void setHasSemicolon(boolean hasSemicolon);
}
