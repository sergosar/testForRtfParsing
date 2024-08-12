package rtfParseKit.MyRtfParseKit;

import rtfParseKit.elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeChanger {
    RootGroup rootGroup;
    final static List<String> variables = new ArrayList<>(Arrays.asList
            ("s_002.ssYppName", "s_002.ssSAPnumber", "s_002.ssnumber", "s_001. sCustomerPersonHL", "s_001. sCustomerPersonHL",
                    "s_001. ssRepNumAggrAgency", "s_001. ddDateAggrAgency", "s_001.sDocMC"));

    public TreeChanger(RootGroup rootGroup) {
        this.rootGroup = rootGroup;
    }

    public static void setNewVariables() {

    }

    public static MyGroup getGroupWithText(MyGroup myGroup, String text) {
        MyGroup result = null;
        for (Writeable w : myGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) {
                if (w.getText().contains(text)) return myGroup;
            }
            if (w instanceof MyGroup) {
                MyGroup res = getGroupWithText((MyGroup) w, text);
                if (res != null) return res;
            }
        }
        return result;
    }

    public static MyGroup getGroupWithStartSymbol(MyGroup myGroup) {
        MyGroup result = null;
        for (Writeable w : myGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) {
                if (w.getText().startsWith("\\")) return myGroup;
            }
            if (w instanceof MyGroup) {
                MyGroup res = getGroupWithStartSymbol((MyGroup) w);
                if (res != null) return res;
            }
        }
        return result;
    }

    public static void changeStringValues(MyGroup rootGroup, List<String> oldVariables, List<String> newVariables) {
        if (oldVariables.size() != newVariables.size() || oldVariables.size() == 0) {
            throw new RuntimeException("Wrong list Variables");
        }
        for (String oldString : oldVariables) {
            changeOneStringValue(rootGroup, oldString, newVariables.remove(0));
        }
    }

    /*меняет текст группе со старого на новый
    предполагается что в группе один MyString
    * */
    public static boolean setGroupNewText(MyGroup group, String oldText, String newText) {
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString && w.getText().equals(oldText)) {
                ((MyString) w).setData(newText);
                return true;
            }

        }
        return false;
    }

    /*меняет текст группе на новый
предполагается что в группе один MyString
* */
    public static void setGroupNewText(MyGroup group, String newText) {
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString) ((MyString) w).setData(newText);
        }
    }

    /*без оптимизации обхода не с начала*/
    public static void changeOneStringValue(MyGroup group, String oldValue, String newValue) {
        MyGroup startGroup = getGroupWithStartSymbol(group);

        String startGroupText = getGroupText(startGroup);
      //  System.out.println("startGroupText.length() = " + startGroupText.length());
        if (startGroupText.endsWith("\\") && startGroupText.length()>2) {
            setGroupNewText(startGroup, oldValue, newValue);
        } else {
            setGroupNewText(startGroup, newValue);
            MyGroup nextGroup = getNextGroup(startGroup.getParentGroup(), startGroup.getGroupIndex());


            //TODO: возможно сделать удаление по пересечению , подумать
            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroup(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                deleteGroup(forDelete);
                if (getGroupText(forDelete).endsWith("\\")) break;
            }
        }


    }

    public static void deleteGroup(MyGroup myGroup) {
        myGroup.getParentGroup().getInnerGroups().remove(myGroup);
    }

    /*предполагается что в группе один MyString */
    public static String getGroupText(MyGroup group) {
        String text = null;
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString) text = w.getText();
        }
        return text;
    }

    public static MyGroup getNextGroup(MyGroup parentGroup, int previousGroupNum) {
        MyGroup result = null;
        for (Writeable w : parentGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) continue;
            if (w instanceof MyGroup) {
                if (((MyGroup) w).getGroupIndex() == previousGroupNum + 1) return (MyGroup) w;
                MyGroup res = getNextGroup((MyGroup) w, previousGroupNum);
                if (res != null) return res;
            }
        }
        return result;
    }

    public static List<Writeable> getCopyPart(MyGroup parentGroup, String startPos, String endPos) {
        return null;
      }
}
