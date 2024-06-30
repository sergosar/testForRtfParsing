package rtfParseKit;

public class DocWriter {
    public static byte[] getISOBytes(String text)
    {
        if (text == null)
            return null;
        int len = text.length();
        byte[] b = new byte[len];
        for (int k = 0; k < len; ++k)
            b[k] = (byte)text.charAt(k);
        return b;
    }
}
