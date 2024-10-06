package rtf.listener;

import com.rtfparserkit.parser.standard.StandardRtfParser;
import com.rtfparserkit.rtf.Command;

public class MyStandardRtfParser extends StandardRtfParser {
    @Override
    public void processCommand(Command command, int parameter, boolean hasParameter, boolean optional) {
        super.processCommand(command, parameter, hasParameter, optional);
    }
}
