package rtf.elements;

/**
 * @author Мирошниченко Сергей
 */
public class RootGroup extends MyGroup {

    public boolean start;

    public RootGroup() {
    }

    @Override
    public String getText() {
        return "This is Root Group";
    }
}
