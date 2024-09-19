package com.example.ide.css;


import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ArrayUtil;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class QScssUtil {

    private static final String COMMA = ",";
    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    private static final String SPACE = " ";
    /**
     * 需要连接父亲的名字
     */
    private static final String CONNECT_FLAG = "&";

    private static final ArrayList<String> realName = new ArrayList<>();

    public static String traverse(String head, List<String> list, int pos) {
        head = StringUtils.trim(head);
        if (head.contains(SPACE)) {
            head = head.split(SPACE)[head.split(SPACE).length-1];
        }
        if(head.contains(DOT) && head.indexOf(DOT)!=0){
            head = DOT + StringUtils.substringAfterLast(head , DOT);
        }
         if (pos >= list.size()) {
            realName.add(head);
            return String.valueOf(EMPTY_STRING);
        }
        final String now = list.get(pos);
        StringBuilder newHead = new StringBuilder(now);
        if (!now.contains(COMMA)) {
            String sb =ArrayUtil.getLastElement(now.split(SPACE));
            sb = sb.replaceFirst(CONNECT_FLAG, head);
            sb += (traverse(sb, list, pos + 1));
            return sb;
        }

        for (String string : newHead.toString().split(COMMA)) {
            string = StringUtils.trim(string);
            traverse(string.replaceAll(CONNECT_FLAG, head), list, pos + 1);
        }
        return newHead.toString();
    }

    private static void init() {
        realName.clear();
    }

    public static ArrayList<String> getOriginCss(List<String> list) {
        init();
        String head =  ContainerUtil.getFirstItem(list);
        String[] strings = head.split(COMMA);
        if (head.contains(SPACE) && !head.contains(COMMA)) {
            String sb = ArrayUtil.getLastElement(head.split(SPACE));
            traverse(sb, list, 1);
        } else {
            for (String string : strings) {
                traverse(string, list, 1);
            }
        }
        return realName;
    }

}