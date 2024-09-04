package rtfParseKit.elements;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HexUtil {
    final Charset utfCharset = StandardCharsets.UTF_8;
    final static Charset windowsCharset = Charset.forName("windows-1251");

    public static String getHex(String s) {

        StringBuilder result = new StringBuilder();
        String temp;

        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) < 128) {
                result.append(s.charAt(i));
            } else {
                temp = String.valueOf(s.charAt(i));

                result.append("\\").append("'").append(Hex.encodeHexString(temp.getBytes(windowsCharset)));
            }
        }
        return result.toString();
    }

    public static String getHex2(String s) {

        StringBuilder result = new StringBuilder();
        String temp;

        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) < 10) {
                temp = String.valueOf(s.charAt(i));
                result.append("\\").append("'").append(Hex.encodeHexString(temp.getBytes(windowsCharset)));
            } else if (s.codePointAt(i) < 128) {
                result.append(s.charAt(i));
            } else {
                if (s.codePointAt(i) > 32768) {
                    temp = "\\u" + (s.codePointAt(i) - 65536) + " ?";
                } else temp = "\\u" + s.codePointAt(i) + " ?";

                result.append(temp);
            }
        }


        return result.toString();
    }
}
