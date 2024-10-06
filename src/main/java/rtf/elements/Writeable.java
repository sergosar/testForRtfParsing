package rtf.elements;

/**
 * @author Мирошниченко Сергей
 */
public interface Writeable {
    byte[] bytesForWriting();

    String getText();

    void setHasSemicolon(boolean hasSemicolon);
}
