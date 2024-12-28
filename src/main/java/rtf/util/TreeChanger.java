package rtf.util;

import com.rtfparserkit.rtf.Command;
import org.apache.commons.lang3.StringUtils;
import rtf.print.PrintContext;
import rtf.elements.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Мирошниченко Сергей
 */
public class TreeChanger {
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private RootGroup rootGroup;

    private final String startGroupTemplate = "\\\\";

    public TreeChanger(RootGroup rootGroup) {
        this.rootGroup = rootGroup;
    }

    public static MyGroup getGroupWithText(MyGroup myGroup, String text) {
        MyGroup result = null;
        for (Writeable w : myGroup.getInnerGroups()) {
            if (w instanceof MyCommand) {
                continue;
            }
            if (w instanceof MyString) {
                if (w.getText().contains(text)) {
                    return myGroup;
                }
            }
            if (w instanceof MyGroup) {
                MyGroup res = getGroupWithText((MyGroup) w, text);
                if (res != null) {
                    return res;
                }
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
            if (w instanceof MyCommand) {
                continue;
            }
            if (w instanceof MyString) {
                if (w.getText().startsWith("\\") || w.getText().endsWith("\\")) {
                    return myGroup;
                }
            }
            if (w instanceof MyGroup) {
                MyGroup res = getGroupWithStartSymbol((MyGroup) w);
                if (res != null) {
                    return res;
                }
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
            if (w instanceof MyCommand) {
                continue;
            }
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


        String template = startGroupTemplate + "Scan(" + scanPar + ")" + startGroupTemplate;
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
            String template = startGroupTemplate + "EndScan(" + scanPar + ")" + startGroupTemplate;
            if (getFullWord(myGroup).replaceAll(" ", "").equals(template)) {
                result = myGroup;
                break;
            }
        }
        return result;
    }

    private MyGroup getStartGroupWithEndScanForFirstScan(RootGroup rootGroup, String scanPar) {
        MyGroup result = null;
        List<MyGroup> groups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, groups);
        int scanDepth = 0;
        for (MyGroup myGroup : groups) {
            String template = (startGroupTemplate + "EndScan(" + scanPar + ")" + startGroupTemplate).toLowerCase();
            String template2 = (startGroupTemplate + "EndScan".toLowerCase() + startGroupTemplate).toLowerCase();
            String testText = getFullWord(myGroup).replaceAll(" ", "").toLowerCase();
            logger.log(Level.INFO, "getStartGroupWithEndScanForFirstScan testText = " + testText);
            if (testText.startsWith(startGroupTemplate + "scan") && !testText.equals(startGroupTemplate + "scan(" + scanPar + ")" + startGroupTemplate)) {
                scanDepth++;
            }
            if ((testText.contains(template) || testText.contains(template2)) && scanDepth == 0) {
                result = myGroup;
                break;
            }
            if (testText.startsWith(startGroupTemplate + "endscan")) {
                scanDepth--;
            }
        }
        return result;
    }

    /**
     * Возвращает первый втреченный в дереве параметр у Scan(параметр)
     *
     * @return
     */
    public String getScanPar() {
        String result = null;
        List<MyGroup> groups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, groups);
        for (MyGroup myGroup : groups) {
            String str = getFullWord(myGroup);
            if (str.replaceAll(" ", "").contains("Scan")) {
                result = StringUtils.substringBetween(str, "(", ")");
                break;
            }
        }
        return result;
    }

    public void changeStringValuesFromMap(MyGroup rootGroup, Map<String, Object> values) {
        var positionGroup = rootGroup;
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
        int strQ = howManyStringsInGroup(group);
        if (strQ == 1) {
            for (Writeable w : group.getInnerGroups()) {
                if (w instanceof MyString && w.getText().equals(oldText)) {
                    ((MyString) w).setData(newText);
                    return true;
                }
            }
        } else {
            for (Writeable w : group.getInnerGroups()) {
                if (w instanceof MyString) {
                    if (strQ > 1) {
                        ((MyString) w).setData("");
                        --strQ;
                    } else {
                        ((MyString) w).setData(newText);
                    }
                }
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
        int strQ = howManyStringsInGroup(group);

        if (strQ == 1) {
            for (Writeable w : group.getInnerGroups()) {
                if (w instanceof MyString) {
                    ((MyString) w).setData(newText);
                }
            }
        } else {
            for (Writeable w : group.getInnerGroups()) {
                if (w instanceof MyString) {
                    if (strQ > 1) {
                        ((MyString) w).setData("");
                        --strQ;
                    } else {
                        ((MyString) w).setData(newText);
                    }
                }
            }
        }
    }

    private static int howManyStringsInGroup(MyGroup group) {
        int q = 0;
        for (Writeable w : group.getInnerGroups()) {
            if (w instanceof MyString) {
                q++;
            }
        }
        return q;
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
                if (getGroupText(forDelete).endsWith("\\")) {
                    break;
                }
            }
        }
        return true;
    }

    public void changeStringValuesFromMapList(RootGroup rootGroup, Map<String, List<Map<String, Object>>> values) {
        List<MyGroup> startGroups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, startGroups);

        Set<String> mapKeys = values.keySet();
        for (MyGroup startGroup : startGroups) {
            String fullWord = getFullWord(startGroup);
            logger.log(Level.OFF, "Полное служебно слово----------------->" + fullWord);
            String regEx = ".*" + startGroupTemplate + startGroupTemplate + ".+(\\.|:).+" + startGroupTemplate + startGroupTemplate + ".*";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(fullWord);
            if (matcher.matches()) {
                String fullWordBetween = (StringUtils.substringBetween(
                        fullWord.replaceAll(" ", ""), startGroupTemplate, startGroupTemplate));
                String postfix = StringUtils.substringAfterLast(fullWord, startGroupTemplate);
                String preffix = StringUtils.substringBefore(fullWord, startGroupTemplate);
                fullWord = startGroupTemplate + fullWordBetween.replaceAll(":", ".") + startGroupTemplate;
                logger.log(Level.OFF, "Обработанное служебное слово----------------->" + fullWord);
                if (mapKeys.contains(fullWordBetween.split("(\\.|:)")[0])) {
                    List<Map<String, Object>> list = values.get(fullWordBetween.split("(\\.|:)")[0]);
                    logger.log(Level.OFF, "mapKeys.contains(fullWordBetween)----------------->" + fullWordBetween);
                    logger.log(Level.OFF, "Utils.toLocalizedString(list.get(0).get(fullWord) " + Utils.toLocalizedString(list.get(0).get(fullWord)));
                    logger.log(Level.OFF, "list.get(0).get(fullWord) " + list.get(0).get(fullWord));
                    changeOneCtrlWord(startGroup, preffix + Utils.toLocalizedString(list.get(0).get(fullWord)) + postfix, fullWord);
                }
            }
        }
    }

    public void changeStringValuesFromMapListBetweenScanEnd(RootGroup rootGroup, String scanPar, Map<String, List<Map<String, Object>>> values) {
        List<MyGroup> startGroups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, startGroups);
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScanForFirstScan(rootGroup, scanPar);
        int startIndex = groupWithScan.getGroupIndex();
        int endIndex = groupWithEndScan.getGroupIndex();

        Set<String> mapKeys = values.keySet();
        for (MyGroup startGroup : startGroups) {
            if (startGroup.getGroupIndex() > startIndex && startGroup.getGroupIndex() < endIndex) {
                String fullWord = getFullWord(startGroup);
                logger.log(Level.OFF, "Полное служебно слово----------------->" + fullWord);
                String regEx = ".*" + startGroupTemplate + startGroupTemplate + ".+(\\.|:).+" + startGroupTemplate + startGroupTemplate + ".*";
                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(fullWord);
                if (matcher.matches()) {
                    String fullWordBetween = (StringUtils.substringBetween(
                            fullWord.replaceAll(" ", ""), startGroupTemplate, startGroupTemplate));
                    String postfix = StringUtils.substringAfterLast(fullWord, startGroupTemplate);
                    String preffix = StringUtils.substringBefore(fullWord, startGroupTemplate);
                    fullWord = startGroupTemplate + fullWordBetween.replaceAll(":", ".") + startGroupTemplate;
                    logger.log(Level.OFF, "Обработанное служебное слово----------------->" + fullWord);
                    if (mapKeys.contains(fullWordBetween.split("(\\.|:)")[0])) {
                        List<Map<String, Object>> list = values.get(fullWordBetween.split("(\\.|:)")[0]);
                        logger.log(Level.OFF, "mapKeys.contains(fullWordBetween)----------------->" + fullWordBetween);
                        logger.log(Level.OFF, "Utils.toLocalizedString(list.get(0).get(fullWord) " + Utils.toLocalizedString(list.get(0).get(fullWord)));
                        logger.log(Level.OFF, "list.get(0).get(fullWord) " + list.get(0).get(fullWord));
                        changeOneCtrlWord(startGroup, preffix + Utils.toLocalizedString(list.get(0).get(fullWord)) + postfix, fullWord);
                    }
                }
            } else if (startGroup.getGroupIndex() > endIndex) {
                break;
            }
        }
    }

    public void changeStringValuesFromContext(RootGroup rootGroup, PrintContext context) {
        List<MyGroup> startGroups = new ArrayList<>();
        getGroupsWithStartSymbol(rootGroup, startGroups);
        for (MyGroup startGroup : startGroups) {
            String fullWord = getFullWord(startGroup);
            logger.log(Level.OFF, "changeStringValuesFromContext FULLWORD----------------->" + fullWord);
            String regEx = ".*" + startGroupTemplate + startGroupTemplate + "[^.|:]+" + startGroupTemplate + startGroupTemplate + ".*";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(fullWord);
            if (matcher.matches()) {
                logger.log(Level.OFF, "changeStringValuesFromContext FULLWORD----------------->" + fullWord);
                String preffix = StringUtils.substringBefore(fullWord, startGroupTemplate);
                String postfix = StringUtils.substringAfterLast(fullWord, startGroupTemplate);
                fullWord = fullWord.replaceAll(" ", "");
                fullWord = StringUtils.substringBetween(fullWord, startGroupTemplate, startGroupTemplate);
                changeOneCtrlWord(startGroup, preffix + Utils.toLocalizedString(context.resolve(fullWord).getValue()) + postfix, fullWord);
            }
        }
    }

    /**
     * Меняет одно контрольное слово fullWord, которое начинается в startGroup на значение value
     *
     * @param startGroup
     * @param value
     * @param fullWord
     */
    private void changeOneCtrlWord(MyGroup startGroup, String value, String fullWord) {
        String startGroupText = getGroupText(startGroup);

        if (startGroupText.endsWith(startGroupTemplate) && startGroupText.length() > 2 && !startGroupText.startsWith(" ")) {
            setGroupNewText(startGroup, fullWord, value);
        } else {
            setGroupNewText(startGroup, value);
            MyGroup nextGroup = getNextGroupByNum(startGroup.getParentGroup(), startGroup.getGroupIndex());
            while (true) {
                MyGroup forDelete = nextGroup;
                nextGroup = getNextGroupByNum(nextGroup.getParentGroup(), nextGroup.getGroupIndex());
                String temp = getGroupText(forDelete);
                if (temp.contains(startGroupTemplate) && temp.length() > startGroupTemplate.length() && !temp.endsWith(startGroupTemplate)) {
                    deletePartOfGroupStrings(forDelete);
                    break;
                }
                if (!groupContainsCommand(forDelete, Command.cell)) {
                    logger.log(Level.OFF, "deleteGroup-> " + forDelete.getParentGroup().getInnerGroups().remove(forDelete));
                } else {
                    setGroupNewText(forDelete, "");
                    break;
                }

                if (getGroupText(forDelete).endsWith("\\")) {
                    break;
                }
            }
        }
    }

    private static void deletePartOfGroupStrings(MyGroup group) {
        Iterator<Writeable> it = group.getInnerGroups().listIterator();
        while (it.hasNext()) {
            Writeable w = it.next();
            if (w instanceof MyString) {
                String temp = w.getText();
                if (temp.contains("\\")) {
                    it.remove();
                    break;
                }
                it.remove();
            }
        }
    }

    private void deleteGroup(MyGroup myGroup) {
        if (!groupContainsCommand(myGroup, Command.cell)) {
            logger.log(Level.OFF, "deleteGroup-> " + myGroup.getParentGroup().getInnerGroups().remove(myGroup));
        } else {
            setGroupNewText(myGroup, "");
        }
    }

    /**
     * Получение текста из группы
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

    /**
     * Формирование полного служебного слова, т.к. слово может быть растянуто на несколько групп
     */
    private String getFullWord(MyGroup groupWithStartSymbol) {
        String startGroupText = getGroupText(groupWithStartSymbol);
        var stringBuilder = new StringBuilder();
        stringBuilder.append(startGroupText);
        var nextGroup = getNextGroup(groupWithStartSymbol);
        while (true) {
            if (nextGroup == null) {
                break;
            }
            if (getGroupText(nextGroup) != null) {
                stringBuilder.append(getGroupText(nextGroup));
            } else {
                break;
            }
            if (getGroupText(nextGroup).contains("\\")) {
                break;
            }
            nextGroup = getNextGroup(nextGroup);
        }
        return stringBuilder.toString();
    }

    private String getFullWord2(MyGroup groupWithStartSymbol) {
        String startGroupText = getGroupText(groupWithStartSymbol);
        var stringBuilder = new StringBuilder();
        stringBuilder.append(startGroupText);
        var nextGroup = getNextGroupWithSameDepthByPosition(groupWithStartSymbol);
        while (true) {
            if (nextGroup == null) {
                break;
            }
            if (getGroupText(nextGroup) != null) {
                stringBuilder.append(getGroupText(nextGroup));
            } else {
                break;
            }
            if (getGroupText(nextGroup).contains("\\")) {
                break;
            }
            nextGroup = getNextGroup(nextGroup);
        }
        return stringBuilder.toString();
    }

    /**
     * Добавление строк между \Scan\ и \Endscan\
     * index - номер предыдущей строки таблицы, с которой будут сливаться новые строки
     */
    private void addTableRows(MyGroup groupWithScan, MyGroup groupWithEndscan, int index, int quantity) {
        MyGroup parent = groupWithScan.getParentGroup();
        MyGroup groupWithPar;
        if (groupContainsCommand(groupWithScan, Command.par)) {
            groupWithPar = groupWithScan;
        } else {
            groupWithPar = getNextGroupWithCommand(groupWithScan, Command.par);
        }
        int startRowIndex = parent.getInnerGroups().indexOf(groupWithPar);
        int finishRowIndex = parent.getInnerGroups().indexOf(groupWithEndscan);
        List<Writeable> tableRow = new ArrayList<>(parent.getInnerGroups().subList(startRowIndex + 1, finishRowIndex));
        for (int i = 0; i < quantity; i++) {
            List<Writeable> tableRow2 = new ArrayList<>();
            for (int j = 0; j < tableRow.size(); j++) {
                tableRow2.add(tableRow.get(j).getCopy());
            }
            int size = tableRow2.size();
            parent.getInnerGroups().addAll(finishRowIndex, tableRow2);
            finishRowIndex += size;
        }
    }

    private void addAndFillRows(MyGroup groupWithScan, MyGroup groupWithEndscan, int quantity, Map<String, List<Map<String, Object>>> valuesMap) {
        MyGroup parent = groupWithScan.getParentGroup();
        MyGroup groupWithPar;
        if (groupContainsCommand(groupWithScan, Command.par)) {
            groupWithPar = groupWithScan;
        } else {
            groupWithPar = getNextGroupWithCommand(groupWithScan, Command.par);
        }

        int startRowIndex = parent.getInnerGroups().indexOf(groupWithPar);
        int finishRowIndex = parent.getInnerGroups().indexOf(groupWithEndscan);

        logger.log(Level.INFO, "addAndFillRows startRowIndex = " +startRowIndex + " finishRowIndex = " + finishRowIndex);
        List<Writeable> tableRow = new ArrayList<>(parent.getInnerGroups().subList(startRowIndex + 1, finishRowIndex));
        List<Writeable> tableRowCopy = new ArrayList<>();
        for (int j = 0; j < tableRow.size(); j++) {
            tableRowCopy.add(tableRow.get(j).getCopy());
        }
        List<MyGroup> startGroups = new ArrayList<>();
        getGroupsWithStartSymbolInList(tableRow, startGroups, parent);

        Set<String> mapKeys = valuesMap.keySet();
        for (int i = 0; i < quantity; i++) {
            List<Writeable> tableRow2 = new ArrayList<>();

            for (int j = 0; j < tableRowCopy.size(); j++) {
                tableRow2.add(tableRowCopy.get(j).getCopy());
            }
            parent.getInnerGroups().addAll(finishRowIndex, tableRow2);
            fillRows(valuesMap, startGroups, mapKeys, i);
            startGroups.clear();
            Utils.fullRefreshIndexes(rootGroup);
            getGroupsWithStartSymbolInList(tableRow2, startGroups, parent);
            finishRowIndex = parent.getInnerGroups().indexOf(groupWithEndscan);
        }
        fillRows(valuesMap, startGroups, mapKeys, quantity);
    }

    private void fillRows(Map<String, List<Map<String, Object>>> valuesMap, List<MyGroup> startGroups, Set<String> mapKeys, int i) {
        for (MyGroup startGroup : startGroups) {


            /**
             * СДЕСь!!!
             */
            String fullWord = getFullWord(startGroup);
            String[] keys = fullWord.split("\\\\+");

            String[] filteredParts = Arrays.stream(keys)
                    .filter(part -> !part.isEmpty())
                    .toArray(String[]::new);
            for (String s : filteredParts) {
                System.out.println("keys = " + s);
            }
            logger.log(Level.OFF, "addAndFillRows FULLWORD----------------->" + fullWord);
            String regEx = ".*" + startGroupTemplate + startGroupTemplate + ".+(\\.|:).+" + startGroupTemplate + startGroupTemplate + ".*";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(fullWord);
            if (matcher.matches()) {
                String fullWordBetween = (StringUtils.substringBetween(
                        fullWord.replaceAll(" ", ""), startGroupTemplate, startGroupTemplate)).
                        split("(\\.|:)")[0];
                String postfix = StringUtils.substringAfterLast(fullWord, startGroupTemplate);
                String preffix = StringUtils.substringBefore(fullWord, startGroupTemplate);
                fullWord = fullWord.replaceAll(" ", "");
                if (mapKeys.contains(fullWordBetween)) {
                    List<Map<String, Object>> list = valuesMap.get(fullWordBetween);
                    logger.log(Level.OFF, "addAndFillRows mapKeys.contains(fullWordBetween)----------------->" + fullWordBetween);
                    logger.log(Level.OFF, "addAndFillRows list.get(i).get(fullWord) " + list.get(i).get(fullWord));
                    changeOneCtrlWord(startGroup, preffix + list.get(i).get(fullWord) + postfix, fullWord);
                }
            }
        }
    }

    private void getGroupsWithStartSymbolInList(List<Writeable> tableRow, List<MyGroup> startGroups, MyGroup parent) {
        for (Writeable w : tableRow) {
            if (w instanceof MyCommand) continue;
            if (w instanceof MyString) {
                if (w.getText().startsWith("\\") || w.getText().endsWith("\\")) {
                    startGroups.add(parent);
                }
            }
            if (w instanceof MyGroup) {
                getGroupsWithStartSymbol((MyGroup) w, startGroups);
            }
        }
    }

    private static MyGroup getNextGroupWithSameDepth(MyGroup previous) {
        MyGroup result = null;
        MyGroup parent = previous.getParentGroup();
        //TO DO переписать для индексов в листе
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
        MyGroup groupWithEndScan = getStartGroupWithEndScanForFirstScan(rootGroup, scanPar);
        addTableRows(groupWithScan, groupWithEndScan, 1, quantity);
    }

    public void addAndFillRowsFromPrintContext(RootGroup rootGroup, String scanPar, int quantity, Map<String, List<Map<String, Object>>> values) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScanForFirstScan(rootGroup, scanPar);
        logger.log(Level.INFO, "addAndFillRowsFromPrintContext groupWithEndScan " + groupWithEndScan.getGroupIndex()+ " "+ groupWithEndScan.getText());
        addAndFillRows(groupWithScan, groupWithEndScan, quantity, values);
    }

    public void deleteGroupsWithScan(RootGroup rootGroup, String scanPar) {
        MyGroup groupWithScan = getStartGroupWithScan(rootGroup, scanPar);
        MyGroup groupWithEndScan = getStartGroupWithEndScanForFirstScan(rootGroup, scanPar);
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
            if (groupContainsCommand(forDelete, Command.par)) {
                break;
            }
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

    private void changeStringsInGroupFromPrintContext(MyGroup myGroup, String[] keys) {
        for (String key : keys) {
            StringBuilder sb = new StringBuilder();
            boolean start = false;
            Writeable startWriteable = null;
            for (Writeable w : myGroup.getInnerGroups()) {
                if(w instanceof MyString) {
                    String temp = w.getText();
                    if(temp.contains(startGroupTemplate)) {
                        start= true;
                        startWriteable = w;
                    }
                }
                if(start) {
                    sb.append(w.getText());
                }
                if (sb.toString().contains(key+startGroupTemplate)){

                }
            }
        }

    }




}
