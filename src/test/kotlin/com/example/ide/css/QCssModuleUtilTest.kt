package com.example.ide.css

import com.intellij.testFramework.fixtures.BasePlatformTestCase

// test for com/example/ide/css/QCssModuleParseUtil.kt

fun nest(list:List<String>): List<String> {
    return getOriginCss(list)
}
class QCssModuleUtilTest : BasePlatformTestCase() {
    private fun showEqAfterSort(res: List<String> , expected: List<String>) {
        assertEquals(res.sorted(), expected.sorted())
    }
    // all test for nest selector
    fun testCommonNest(){
        showEqAfterSort(nest(listOf(".foo" , "&-bar")), listOf(".foo-bar"))
        showEqAfterSort(nest(listOf(".foo,.foo2" , "&-bar")), listOf(".foo-bar", ".foo2-bar"))
    }
    fun testMultiFatherNest(){
        showEqAfterSort(nest(listOf(".foo,.foo2" , "&-bar", "&-baz")), listOf(".foo-bar-baz", ".foo2-bar-baz"))
    }

    fun testDeepNest(){

        showEqAfterSort(nest(listOf(".foo", "&-bar", "&-baz")), listOf(".foo-bar-baz"))
        showEqAfterSort(nest(listOf(".foo", "&-bar", "&-baz", "&-qux", "&-quux")), listOf(".foo-bar-baz-qux-quux"))
        showEqAfterSort(nest(listOf(".foo", "&-bar", "&-baz", "&-qux", "&-quux", "&-corge")), listOf(".foo-bar-baz-qux-quux-corge"))
    }

    fun testEmptyNest(){
        showEqAfterSort(nest(listOf()), listOf())
    }

    fun testSingleNest(){
        showEqAfterSort(nest(listOf(".foo")), listOf(".foo"))
    }


    fun testSpaceInNest(){
        showEqAfterSort(nest(listOf(".foo", "&-bar &-baz", "&-qux")), listOf(".foo-baz-qux"))
    }

}