package rtfParseKit.elements;

public class RootGroup extends MyGroup{

    private static RootGroup rootGroup;
    public boolean start;

    private RootGroup() {
    }

    public static RootGroup getInstance() {
        if(rootGroup==null) {
            rootGroup =  new RootGroup();
        }
        return rootGroup;
    }

    @Override
    public String getText() {
        return "This is Root Group";
    }
}
