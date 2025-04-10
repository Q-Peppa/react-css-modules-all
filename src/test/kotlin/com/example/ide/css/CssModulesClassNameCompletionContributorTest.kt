package com.example.ide.css

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CssModulesClassNameCompletionContributorTest : BasePlatformTestCase() {
    fun testCssCompletion() {
        println("enter")
        myFixture.configureByText(
            "kw.css", """
            .app {}
            .app-demo{}
        """.trimIndent()
        )
        myFixture.configureByText(
            "hello.js", """
            import cs from './kw.css';
            const a = cs.<caret>;
        """.trimIndent()
        )
        println(myFixture.testDataPath)
        myFixture.completeBasic()
        assertNotNull(myFixture.lookupElementStrings);
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "app", "appDemo", "'app-demo'"
        )
    }
}