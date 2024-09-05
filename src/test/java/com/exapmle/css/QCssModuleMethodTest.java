package com.exapmle.css;

import com.example.css.QCssModulesUtil;
import org.junit.Assert;
import org.junit.Test;

public class QCssModuleMethodTest {


    @Test
    public void testStringRemoveFrom(){
        String world = QCssModulesUtil.StringRemoveFrom("hello world", 5);
        Assert.assertEquals(world , String.valueOf("hello"));

        world = QCssModulesUtil.StringRemoveFrom("hello world", 0);

        Assert.assertEquals(world, "");

        world = QCssModulesUtil.StringRemoveFrom("hello world", Integer.MIN_VALUE);
        Assert.assertEquals(world, "");

        world = QCssModulesUtil.StringRemoveFrom("hello world",  Integer.MAX_VALUE);
        Assert.assertEquals(world, "hello world");
    }
}
