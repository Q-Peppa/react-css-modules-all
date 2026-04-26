package com.peppa.css.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ImportedStylesheetCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/resources"

    private fun assertAndPrintCompletion(expected: List<String>) {
        val actual = myFixture.lookupElementStrings
        println("actual completion strings: ${actual?.sorted()}")
        println("expected completion strings: ${expected.sorted()}")
        assertNotNull(actual)
        assertContainsElements(actual!!, *expected.toTypedArray())
    }

    fun testCssImportCompletionIncludesImportedSelectors() {
        myFixture.copyFileToProject("imports/main.css")
        myFixture.copyFileToProject("imports/imported.css")

        myFixture.configureByText(
            "test.js", """
            import s from "./imports/main.css";
            s['<caret>'];
        """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC)

        assertAndPrintCompletion(listOf("localCard", "sharedCard", "importedCard"))
    }

    fun testCircularCssImportsDoNotLoopAndStillResolveSelectors() {
        myFixture.copyFileToProject("imports/cycle-a.css")
        myFixture.copyFileToProject("imports/cycle-b.css")

        myFixture.configureByText(
            "test.js", """
            import s from "./imports/cycle-a.css";
            s['<caret>'];
        """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC)

        assertAndPrintCompletion(listOf("cycleA", "cycleB"))
    }
}
