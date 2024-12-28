package rtf.print;

import java.net.HttpCookie;

public class PrintContext {
    public Inner resolve(String fullWord) {
        return new Inner();
    }

    public class Inner {
        public String getValue(){
            return "resolve";
        }
    }


}
