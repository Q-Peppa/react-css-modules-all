package com.example.css;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    private static final HashSet<String> restCssNameSet = new HashSet<>();
    private static final HashSet<String> set = new HashSet<>();
    private static final ArrayList<String> realName = new ArrayList<>();

    public static String traverse(String head, List<String> list, int pos) {
        head = head.trim();
        if (head.contains(" ")) {
            head = head.split(SPACE)[head.split(SPACE).length-1];
        }
        if (pos >= list.size()) {
            realName.add(head);
            return String.valueOf(EMPTY_STRING);
        }
        final String now = list.get(pos);
        StringBuilder newHead = new StringBuilder(now);
        if (!now.contains(",")) {
            String sb = now.split(SPACE)[now.split(SPACE).length - 1];
            sb = sb.replaceFirst(CONNECT_FLAG, head);
            for (String inner : now.split(SPACE)) {
                restCssNameSet.add(inner.replace(CONNECT_FLAG, head));
            }
            sb += (traverse(sb, list, pos + 1));
            return sb;
        }

        for (String string : newHead.toString().split(COMMA)) {
            string = string.trim();
            if (string.startsWith(DOT)) set.add(string);
            if (string.startsWith(CONNECT_FLAG)) {
                set.add(string.replaceFirst(CONNECT_FLAG, head));
            }
            traverse(string.replaceAll(CONNECT_FLAG, head), list, pos + 1);
        }
        return newHead.toString();
    }

    private static void init() {
        restCssNameSet.clear();
        set.clear();
        realName.clear();
    }

    public static ArrayList<String> getOriginCss(List<String> list) {
        init();
        String head = list.get(0);
        String[] strings = head.split(COMMA);
        if (head.contains(SPACE) && !head.contains(COMMA)) {
            String sb = head.split(SPACE)[head.split(SPACE).length - 1];
            for (String inner : head.split(SPACE)) {
                restCssNameSet.add(inner.replace(CONNECT_FLAG, head));
            }
            traverse(sb, list, 1);

        } else {
            for (String string : strings) {
                traverse(string, list, 1);
            }
        }
        return realName;
    }

}