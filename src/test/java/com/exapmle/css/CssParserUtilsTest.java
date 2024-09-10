package com.exapmle.css;

import com.example.css.QScssUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class CssParserUtilsTest {
    static void testCase(List<String> s1, List<String> s2){
        ArrayList<String> originCss = QScssUtil.getOriginCss(s1);
        Assert.assertEquals(originCss.stream().sorted().toList(), s2.stream().sorted().toList());
    }
    @Test
    public  void  testSimpleFatherSelector(){
        testCase(List.of(".foo" ,"&-bar") , List.of(".foo-bar"));
        testCase(List.of(".foo" ,"&-bar", "&-zoo") , List.of( ".foo-bar-zoo"));
        testCase(List.of(".foo" ,"&-bar", "&-zoo" , "&-deep") , List.of(".foo-bar-zoo-deep"));
    }

    @Test
    public void testMultiFatherSelector(){
        testCase(List.of(".foo,.bar" ,"&-inner") , List.of(".foo-inner", ".bar-inner"));
        testCase(List.of(".foo,.bar,.baz" ,"&-inner") , List.of(".foo-inner", ".bar-inner", ".baz-inner"));
        testCase(List.of(".foo,.bar" ,"&-inner1,&-inner2") , List.of(".foo-inner1", ".bar-inner1", ".foo-inner2", ".bar-inner2"));
    }

    @Test
    public void testSpaceText(){
        testCase(List.of(".foo .bar" ,"&-inner") , List.of(".bar-inner"));
        testCase(List.of(".foo .bar .space" ,"&-inner") , List.of(".space-inner"));
    }


    @Test
    public void testTagSelectorText(){
        testCase(List.of("div.app" ,"&-inner") , List.of(".app-inner"));
        testCase(List.of("div.app,.foo" ,"&-inner") , List.of(".app-inner", ".foo-inner"));
    }
}
