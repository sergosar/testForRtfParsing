package rtf.util;

import com.rtfparserkit.rtf.Command;
import rtf.elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Мирошниченко Сергей
 */
public class TreeChanger {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private RootGroup rootGroup;

    public TreeChanger(RootGroup rootGroup) {
        this.rootGroup = rootGroup;
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
                if (w.getText().startsWith("\\") || w.getText().endsWith("\\")) return myGroup;
            }
            if (w instanceof MyGroup) {
                MyGroup res = getGroupWithStartSymbol((MyGroup) w);
                if (res != null) return res;
            }
        }
        return result;
    }

    /**
     * Заполняет List groups списком групп со стартовыми символами
     *
     * @param myGroup
     * @param groups
     */
    private static void getGroupsWithStartSymbol(MyGroup myGroup, List<MyGroup> groups) {
        for (Writeable w : myGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) {
                if (w.getText().startsWith("\\") || w.getText().endsWith("\\")) {
                    groups.add(myGroup);
                }
            }
            if (w instanceof MyGroup) {
                getGroupsWithStartSymbol((MyGroup) w, groups);
            }
        }
    }

    private MyGroup getStartGroupWithScan(RootGroup rootGroup, String scanPar) {
        MyGroup result = null;
        List<MyGroup> groups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, groups);

        for (MyGroup group : groups) {

            System.out.println(getGroupText(group) + "   " + getFullWord(group));
            System.out.println("index = " + group.getGroupIndex());
            System.out.println();
        }
        // внимательно насчет количества "\\"
        String template = "\\\\Scan(" + scanPar + ")\\\\";
        for (MyGroup myGroup : groups) {
            String str = getFullWord(myGroup);
            if (str.replaceAll(" ", "").equals(template)) {
                result = myGroup;
                break;
            }
        }
        return result;
    }

    private MyGroup getStartGroupWithEndScan(RootGroup rootGroup, String scanPar) {
        MyGroup result = null;
        List<MyGroup> groups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, groups);
        for (MyGroup myGroup : groups) {
            //         String str = getFullWord(myGroup);
            // внимательно насчет количества "\\"
            if (getFullWord(myGroup).replaceAll(" ", "").equals("\\\\EndScan(" + scanPar + ")\\\\")) {
                result = myGroup;
                break;
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
    public void changeStringValues(MyGroup rootGroup, List<String> oldVariables, List<String> newVariables) {
        if (oldVariables.size() != newVariables.size() || oldVariables.size() == 0) {
            throw new RuntimeException("Wrong list Variables");
        }
        var positionGroup = rootGroup;
        for (String oldString : oldVariables) {
            while (true) {
                if (changeOneStringValue(positionGroup, oldString, newVariables.get(0))) {
                    logger.log(Level.FINE, "changeOneStringValue " + "oldString : " + oldString + "newVariable:  " + newVariables.get(0));
                    newVariables.remove(0);
                    break;
                }
                positionGroup = getNextGroup(positionGroup);
            }
        }
    }

    public void changeStringValues(MyGroup rootGroup, Map<String, Object> values) {
//        if (oldVariables.size() != newVariables.size() || oldVariables.size() == 0) {
//            throw new RuntimeException("Wrong list Variables");
//        }
        var positionGroup = rootGroup;
        for (String oldString : values.keySet()) {
            var suc = false;
            if (values.get(oldString) != null) {
                suc = changeOneStringValue(positionGroup, oldString, values.get(oldString).toString());
            }

            logger.log(Level.WARNING, "var suc = " + suc);
            if (values.get(oldString) != null)
                logger.log(Level.FINE, "changeOneStringValue " + "oldString : " + oldString + "newVariable:  " + values.get(oldString).toString());


            positionGroup = getNextGroup(positionGroup);
        }
    }


    public void changeStringValuesFromMap(MyGroup rootGroup, Map<String, Object> values) {
        var positionGroup = rootGroup;
//        for (String oldString : values.keySet()) {
//            var suc = false;
//            if (values.get(oldString) != null) {
//                suc = changeOneStringValue(positionGroup, oldString, values.get(oldString).toString());
//            }
//
//            logger.log(Level.WARNING, "var suc = " + suc);
//            if (values.get(oldString) != null)
//                logger.log(Level.FINE, "changeOneStringValue " + "oldString : " + oldString + "newVariable:  " + values.get(oldString).toString());
//
//
//            positionGroup = getNextGroup(positionGroup);
//        }
        do {
            changeOneStringValueFromMap(positionGroup, values);
            positionGroup = getNextGroup(positionGroup);
        } while (positionGroup != null);
    }

    /**
     * меняет текст группе со старого на новый
     * предполагается что в группе один MyString
     *
     * @param group
     * @param oldText
     * @param newText
     */
    public static boolean setGroupNewText(MyGroup group, String oldText, String newText) {
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString && w.getText().equals(oldText)) {
                ((MyString) w).setData(newText);
                return true;
            }
        }
        return false;
    }

    /**
     * меняет текст группе со старого на новый
     * предполагается что в группе один MyString
     *
     * @param group
     * @param newText
     */
    public static void setGroupNewText(MyGroup group, String newText) {
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString) ((MyString) w).setData(newText);
        }
    }

    public boolean changeOneStringValue(MyGroup group, String oldValue, String newValue) {
        logger.log(Level.WARNING, "changeOneStringValue: " + "oldValue= " + oldValue + "newValue= " + newValue);
        /*без оптимизации обхода не с начала*/
        MyGroup startGroup = getGroupWithStartSymbol(group);
        if (startGroup == null) {
            return false;
        }
        if (!getFullWord(startGroup).equals(oldValue)) {
            logger.log(Level.WARNING, "getFullWord= " + getFullWord(startGroup));
            return false;
        }

        String startGroupText = getGroupText(startGroup);
        logger.log(Level.FINE, "startGroupText.length() = " + startGroupText.length());

        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
            setGroupNewText(startGroup, oldValue, newValue);
        } else {
            setGroupNewText(startGroup, newValue);
            MyGroup nextGroup = getNextGroupByNum(startGroup.getParentGroup(), startGroup.getGroupIndex());

            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                deleteGroup(forDelete);
                if (getGroupText(forDelete).endsWith("\\")) break;
            }
        }
        return true;
    }

    private boolean changeOneStringValueFromMap(MyGroup group, Map<String, Object> values) {

        MyGroup startGroup = getGroupWithStartSymbol(group);
        if (startGroup == null) {
            return false;
        }
        String fullWord = getFullWord(startGroup);

        if (!values.containsKey(fullWord)) {
            logger.log(Level.WARNING, "getFullWord= " + fullWord);
            return false;
        }

        String startGroupText = getGroupText(startGroup);
        logger.log(Level.FINE, "startGroupText.length() = " + startGroupText.length());

        var valueFromKey = values.get(fullWord);
        if (valueFromKey == null) {
            valueFromKey = "";
        }
        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
            setGroupNewText(startGroup, fullWord, valueFromKey.toString());
        } else {
            setGroupNewText(startGroup, valueFromKey.toString());
            MyGroup nextGroup = getNextGroupByNum(startGroup.getParentGroup(), startGroup.getGroupIndex());

            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                deleteGroup(forDelete);
                if (getGroupText(forDelete).endsWith("\\")) break;
            }
        }
        return true;
    }

    public boolean changeOneStringValueFromMapList(MyGroup group, Map<String, List<Object>> values) {

        MyGroup startGroup = getGroupWithStartSymbol(group);
        if (startGroup == null) {
            return false;
        }
        String fullWord = getFullWord(startGroup);

        if (!values.containsKey(fullWord)) {
            logger.log(Level.WARNING, "getFullWord= " + fullWord);
            return false;
        }

        String startGroupText = getGroupText(startGroup);
        logger.log(Level.FINE, "startGroupText.length() = " + startGroupText.length());

        var valueFromKey = values.get(fullWord).remove(0);

        if (valueFromKey == null) {
            valueFromKey = "";
        }

        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
            setGroupNewText(startGroup, fullWord, valueFromKey.toString());
        } else {
            setGroupNewText(startGroup, valueFromKey.toString());
            MyGroup nextGroup = getNextGroupByNum(startGroup.getParentGroup(), startGroup.getGroupIndex());

            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                deleteGroup(forDelete);
                if (getGroupText(forDelete).endsWith("\\")) break;
            }
        }
        return true;
    }

    public void changeStringValuesFromMapList(RootGroup rootGroup, String scanPar, Map<String, List<Object>> values) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScan(rootGroup, scanPar);

    }

    private static void deleteGroup(MyGroup myGroup) {
        myGroup.getParentGroup().getInnerGroups().remove(myGroup);
    }

    /**
     * Получение текста из группы
     * предполагается что в группе один MyString, если несколько берется первый
     *
     * @param group
     * @return текст из группы
     */
    public static String getGroupText(MyGroup group) {
        StringBuilder text = new StringBuilder();

        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString) {
                text.append(w.getText());

            }
        }
        if (text.length() > 0) {
            return text.toString();
        } else {
            return null;
        }

    }

    /**
     * @param parentGroup
     * @param previousGroupNum
     * @return группу слудующюю по индексу за previousGroupNum
     */
    private static MyGroup getNextGroupByNum(MyGroup parentGroup, int previousGroupNum) {
        MyGroup result = null;
        for (Writeable w : parentGroup.getInnerGroups()) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) continue;
            if (w instanceof MyGroup) {
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
    private MyGroup getNextGroup(MyGroup startGroup) {
        if (startGroup == null) return null;
        return getNextGroupByNum(rootGroup, startGroup.getGroupIndex());
    }

    public static List<Writeable> getCopyPart(MyGroup parentGroup, String startPos, String endPos) {
        return null;
    }

    /**
     * Формирование полного служебного слова, т.к. слово может быть растянуто на несколько групп
     */
    private String getFullWord(MyGroup groupWithStartSymbol) {
        String startGroupText = getGroupText(groupWithStartSymbol);
//        if (startGroupText.endsWith("\\") && startGroupText.length() > 2) {
//            return startGroupText;
//        }
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

            if (getGroupText(nextGroup).contains("\\")) {
                break;
            }
            nextGroup = getNextGroup(nextGroup);
        }
        return stringBuilder.toString();
    }

    /**
     * @param groupWithScanText
     * @param groupWithEndScanText
     */
    public void scanCopy(String groupWithScanText, String groupWithEndScanText) {

        MyGroup groupWithScan = TreeChanger.getGroupWithText(rootGroup, groupWithScanText);

        MyGroup groupWithEndScan = TreeChanger.getGroupWithText(rootGroup, groupWithEndScanText);
        MyGroup copyText = new MyGroup();
        MyGroup parent = groupWithEndScan.getParentGroup();
        MyGroup parent2 = groupWithScan.getParentGroup();
        MyGroup groupAfterGroupWithScan = getNextGroupWithSameDepth(groupWithScan);
        int indexStart = groupWithEndScan.getParentGroup().getInnerGroups().indexOf(groupAfterGroupWithScan);
        logger.log(Level.FINE, "indexStart : " + indexStart);

        MyGroup groupBeforeGroupWithEndScan = getNextGroupWithSameDepth(groupWithScan);
        int indexEnd = groupWithEndScan.getParentGroup().getInnerGroups().indexOf(groupWithEndScan);
        logger.log(Level.FINE, "indexEnd : " + indexEnd);
        copyText.getInnerGroups().addAll(groupWithEndScan.getParentGroup().getInnerGroups().subList(indexStart, indexEnd));
        groupWithScan.getParentGroup().getInnerGroups().addAll(indexEnd, copyText.getInnerGroups());
        logger.log(Level.FINE, "");
    }

    /**
     * Добавление строк между \Scan\ и \Endscan\
     * index - номер предыдущей строки таблицы, с которой будут сливаться новые строки
     */
    public void addTableRows(MyGroup groupWithScan, MyGroup groupWithEndscan, int index, int quantity) {

        MyGroup parent = groupWithScan.getParentGroup();
        MyGroup groupWithPar;
        if (groupContainsCommand(groupWithScan, Command.par)) {
            groupWithPar = groupWithScan;
        } else {
            groupWithPar = getNextGroupWithCommand(groupWithScan, Command.par);
        }

        int startRowIndex = parent.getInnerGroups().indexOf(groupWithPar);
        int finishRowIndex = parent.getInnerGroups().indexOf(groupWithEndscan);

        List<Writeable> tableRow = parent.getInnerGroups().subList(startRowIndex + 1, finishRowIndex);
        List<Writeable> tableRow2 = new ArrayList<>(tableRow);

        int size = tableRow2.size();

        for (int i = 0; i < quantity; i++) {
            parent.getInnerGroups().addAll(finishRowIndex, tableRow2);
            finishRowIndex += size;
        }
    }

    //TO DO переписать для индексов в листе
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

    private static MyGroup getNextGroupWithSameDepthByPosition(MyGroup previous) {
        MyGroup result = null;
        MyGroup parent = previous.getParentGroup();
        int startIndex = parent.getInnerGroups().indexOf(previous);
        for (int i = startIndex + 1; i < parent.getInnerGroups().size(); i++) {
            if (parent.getInnerGroups().get(i) instanceof MyGroup) {
                result = (MyGroup) parent.getInnerGroups().get(i);
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


    /**
     * Меняет индексы комманд \irow \irowband для групы которая описывает? строку таблицы
     */
    public void setRowIndexForTableGroup(MyGroup group, int index) {
        for (Writeable writeable : group.getInnerGroups()) {
            MyCommand command = (MyCommand) writeable;
            if (command.getCommand() == Command.irow || command.getCommand() == Command.irowband) {
                command.setI(index);
            }
            if (command.getCommand() == Command.irowband) {
                break;
            }
        }
    }

    /**
     * Добавляет метку(комманду) \lastrow к group что предыдущая строка таблицы была последняя
     *
     * @param group
     */
    public void setLastRow(MyGroup group) {
        for (Writeable writeable : group.getInnerGroups()) {
            MyCommand command = (MyCommand) writeable;
            if (command.getCommand() == Command.irowband) {
                int index = group.getInnerGroups().indexOf(writeable);
                group.getInnerGroups().add(index + 1, new MyCommand(Command.lastrow, 0, false, false));
                break;
            }
        }
    }

    /**
     * Убирает метку(комманду) \lastrow у group что предыдущая строка таблицы была последняя
     *
     * @param group
     */
    public void setNoLastRow(MyGroup group) {
        for (Writeable writeable : group.getInnerGroups()) {
            MyCommand command = (MyCommand) writeable;
            if (command.getCommand() == Command.lastrow) {
                group.getInnerGroups().remove(writeable);
                break;
            }
        }
    }


    /**
     * Получает следущуюю группу за myGroup в которой есть комманда command
     */
    private MyGroup getNextGroupWithCommand(MyGroup myGroup, Command command) {
//        MyGroup result = getNextGroupWithSameDepth(myGroup);
        MyGroup result = getNextGroupWithSameDepthByPosition(myGroup);
        if (groupContainsCommand(result, command)) {
            return result;
        } else {
            return getNextGroupWithCommand(result, command);
        }
    }

    /**
     * Содержит ли группа комманду command
     *
     * @return
     */
    private boolean groupContainsCommand(MyGroup group, Command command) {
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyCommand && ((MyCommand) w).getCommand() == command) {
                return true;
            }
        }
        return false;
    }

    public void addRows(RootGroup rootGroup, String scanPar, int quantity) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScan(rootGroup, scanPar);
        addTableRows(groupWithScan, groupWithEndScan, 1, quantity);
    }

    public void deleteGroupsWithScan(RootGroup rootGroup, String scanPar) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScan(rootGroup, scanPar);
        deleteGroupWithCtrlWord(rootGroup, groupWithScan);
        deleteGroupWithCtrlWord(rootGroup, groupWithEndScan);
    }

    private void deleteGroupWithCtrlWord(RootGroup rootGroup, MyGroup group) {
        MyGroup nextGroup = getNextGroupByNum(group.getParentGroup(), group.getGroupIndex());
        deleteGroup(group);
        while (true) {
            MyGroup forDelete = nextGroup;
            nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
            deleteGroup(forDelete);
            //          if (getGroupText(forDelete).contains("\\")) break;
            if (groupContainsCommand(nextGroup, Command.par)) {
                deleteGroup(nextGroup);
                break;
            }
        }

    }

    /**
     * Проверяет что между Scan и EndScan находятся только строка таблицы, которую нужно копировать.
     *
     * @param rootGroup
     * @param scanPar
     * @return
     */
    public boolean isOnlyTableRowsBetweenScans(RootGroup rootGroup, String scanPar) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScan(rootGroup, scanPar);
        if (groupContainsCommand(groupWithScan, Command.par)) {
            return isNextCommandCellOrPar(groupWithScan);
        }
        MyGroup startGroup = getNextGroupWithSameDepthWithCommand(groupWithScan, Command.par);
        return isNextCommandCellOrPar(startGroup);
    }

    /**
     * возвращает true когда следущая команда cell, false когда par
     */
    private boolean isNextCommandCellOrPar(MyGroup startGroup) {
        MyGroup testGroup = getNextGroupWithSameDepthByPosition(startGroup);
        while (testGroup != null) {
            if (groupContainsCommand(testGroup, Command.cell)) {
                return true;
            } else if (groupContainsCommand(testGroup, Command.par)) {
                return false;
            }
            testGroup = getNextGroupWithSameDepthByPosition(testGroup);
        }
        return false;
    }

    private MyGroup getNextGroupWithSameDepthWithCommand(MyGroup startGroup, Command command) {
        MyGroup testGroup = getNextGroupWithSameDepthByPosition(startGroup);
        while (testGroup != null) {
            if (groupContainsCommand(testGroup, command)) {
                return testGroup;
            }
            testGroup = getNextGroupWithSameDepthByPosition(testGroup);
        }
        return null;

    }
}
