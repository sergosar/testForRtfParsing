package rtfParseKit.elements;

import java.nio.charset.StandardCharsets;

public class MyString implements Writeable {
    String data;

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

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
