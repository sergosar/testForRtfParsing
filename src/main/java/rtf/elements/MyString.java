package rtf.elements;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Мирошниченко Сергей
 */
public class MyString implements Writeable {
    private String data;

    public MyString() {
    }

    public MyString(String data) {
        this.data = data;
    }

    @Override
    public byte[] bytesForWriting() {
        return HexUtil.getHex(data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getText() {
        return data;
    }

    @Override
    public void setHasSemicolon(boolean hasSemicolon) {
        // к другим элементам(где встречается точка с запятой) удобнее обращаться через интерфейс,
        // когда строка сама равна ; , то для простоты обработки и меньшего количества строк в группе,
        // она добавляется к предшествующему элементу в парсере
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = Objects.requireNonNullElse(data, "");
    }

    @Override
    public Writeable getCopy() {
        return new MyString(this.data);
    }
}
