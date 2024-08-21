package rtfParseKit.MyRtfParseKit;

import rtfParseKit.elements.*;

import java.util.*;

public class TreeChanger {
    static RootGroup rootGroup;
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
        if (myGroup == null) {
            return result;
        }
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


    /**
     * Заменяет список из специальных служебных слов на новые значения.
     * Порядок специальных слов и соответствующих им служебных значений в списках должен совпадать.
     *
     * @param rootGroup
     * @param oldVariables
     * @param newVariables
     */
    public static void changeStringValues(MyGroup rootGroup, List<String> oldVariables, List<String> newVariables) {
        if (oldVariables.size() != newVariables.size() || oldVariables.size() == 0) {
            throw new RuntimeException("Wrong list Variables");
        }
        var positionGroup = rootGroup;
        for (String oldString : oldVariables) {
            while (true) {
                if (changeOneStringValue(positionGroup, oldString, newVariables.get(0))) {
                    System.out.println("changeOneStringValue " + "oldString : " + oldString + "newVariable:  " + newVariables.get(0));
                    newVariables.remove(0);

                    break;
                }
                positionGroup = getNextGroup(positionGroup);
            }

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
    public static boolean changeOneStringValue(MyGroup group, String oldValue, String newValue) {
        MyGroup startGroup = getGroupWithStartSymbol(group);
        if (startGroup == null) {
            return false;
        }
        if (!getFullWord(startGroup).equals(oldValue)) {
            return false;
        }

        String startGroupText = getGroupText(startGroup);
        //  System.out.println("startGroupText.length() = " + startGroupText.length());
        //проверка на правильное содержание нужного текста

        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
            setGroupNewText(startGroup, oldValue, newValue);
        } else {
            setGroupNewText(startGroup, newValue);
            MyGroup nextGroup = getNextGroupByNum(startGroup.getParentGroup(), startGroup.getGroupIndex());


            //TODO: возможно сделать удаление по пересечению , подумать
            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                deleteGroup(forDelete);
                if (getGroupText(forDelete).endsWith("\\")) break;
            }
        }
        return true;
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

    /**
     * @param parentGroup
     * @param previousGroupNum
     * @return группу слудующюю по индексу за previousGroupNum
     */
    public static MyGroup getNextGroupByNum(MyGroup parentGroup, int previousGroupNum) {
        MyGroup result = null;
        for (Writeable w : parentGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) continue;
            if (w instanceof MyGroup) {
//                if (((MyGroup) w).getGroupIndex() == previousGroupNum + 1) {
//                    return (MyGroup) w;
//                }
                if (((MyGroup) w).getGroupIndex() > previousGroupNum) {
                    return (MyGroup) w;
                }
                MyGroup res = getNextGroupByNum((MyGroup) w, previousGroupNum);
                if (res != null) return res;
            }
        }
        return result;
    }

    /**
     * @param startGroup
     * @return следующюю за startGroup
     */
    private static MyGroup getNextGroup(MyGroup startGroup) {
        if (startGroup == null) return null;
        return getNextGroupByNum(rootGroup, startGroup.getGroupIndex());
    }

    public static List<Writeable> getCopyPart(MyGroup parentGroup, String startPos, String endPos) {
        return null;
    }

    /**
     * Формирование полного служебного слова, т.к. слово может быть растянуто на несколько групп
     */
    private static String getFullWord(MyGroup groupWithStartSymbol) {
        String startGroupText = getGroupText(groupWithStartSymbol);
        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
            return startGroupText;
        }
        var stringBuilder = new StringBuilder();
        stringBuilder.append(startGroupText);
        var nextGroup = getNextGroup(groupWithStartSymbol);
        while (true) {
            if (nextGroup == null) {
                break;
            }
            if (getGroupText(nextGroup) != null) {
                stringBuilder.append(getGroupText(nextGroup));
            } else break;

            if (getGroupText(nextGroup).endsWith("\\")) {
                break;
            }
            nextGroup = getNextGroup(nextGroup);
        }
        return stringBuilder.toString();
    }

    public static void scanCopy() {
        MyGroup groupWithScan = TreeChanger.getGroupWithText(rootGroup, "\\scan(s_002)\\");

        MyGroup groupWithEndScan = TreeChanger.getGroupWithText(rootGroup, "\\EndScan\\");
        MyGroup copyText = new MyGroup();
        MyGroup parent = groupWithEndScan.getParentGroup();
        MyGroup parent2 = groupWithScan.getParentGroup();
        MyGroup groupAfterGroupWithScan = getNextGroupWithSameDepth(groupWithScan);
        int indexStart = groupWithEndScan.getParentGroup().getInnerGroups().indexOf(groupAfterGroupWithScan);
        System.out.println("indexStart : " + indexStart);

        MyGroup groupBeforeGroupWithEndScan = getNextGroupWithSameDepth(groupWithScan);
        int indexEnd = groupWithEndScan.getParentGroup().getInnerGroups().indexOf(groupWithEndScan);
        System.out.println("indexEnd : " + indexEnd);
        copyText.getInnerGroups().addAll(groupWithEndScan.getParentGroup().getInnerGroups().subList(indexStart,indexEnd));
        groupWithScan.getParentGroup().getInnerGroups().addAll(indexEnd, copyText.getInnerGroups());
        System.out.println();
    }

//    /**
//     * Формирование
//     */
//    private static MyGroup copyPart(MyGroup from, MyGroup to) {
//        return;
//    }

    ;

    private static MyGroup getNextGroupWithSameDepth(MyGroup previous) {
        MyGroup result = null;
        MyGroup parent = previous.getParentGroup();
        for (Writeable w : parent.getInnerGroups()) {
            if (w instanceof MyGroup && ((MyGroup) w).getGroupIndex() > previous.getGroupIndex()) {
                result = (MyGroup) w;
                break;
            }
        }
        return result;
    }

    private static MyGroup getPreviousGroupWithSameDepth(MyGroup next) {
        MyGroup result = null;
        MyGroup parent = next.getParentGroup();
        ListIterator<Writeable> it = parent.getInnerGroups().listIterator(parent.getInnerGroups().size());
        while (it.hasPrevious()) {
            Writeable w = it.previous();
            if (w instanceof MyGroup && ((MyGroup) w).getGroupIndex() < next.getGroupIndex()) {
                result = (MyGroup) w;
                break;
            }
        }
        return result;
    }
}

