package com.example.ide.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase


class CssModulesClassNameCompletionContributorTest : BasePlatformTestCase() {

    fun testCssCompletion() {
        myFixture.copyFileToProject("deep/deep_nest.right.css")
        myFixture.copyFileToProject("1.js")
        myFixture.configureByFile("1.js")
        myFixture.complete(CompletionType.BASIC)
        assertNotNull(myFixture.lookupElementStrings)
        assertNotEmpty(myFixture.lookupElementStrings)
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "level1",
            "level2",
            "level2__item",
            "level3",
            "level4",
            "level5",
            "level6",
            "level7",
            "level8",
            "level9",
            "level10",
            "'level-1'",
            "'level-2'",
            "'level-2__item'",
            "'level-3'",
            "'level-4'",
            "'level-5'",
            "'level-6'",
            "'level-7'",
            "'level-8'",
            "'level-9'",
            "'level-10'",
        )
    }

    override fun getTestDataPath() = "src/test/resources"
}