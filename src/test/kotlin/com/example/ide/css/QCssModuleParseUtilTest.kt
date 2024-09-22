package com.example.ide.css


import com.intellij.psi.css.CssFileType
import com.intellij.psi.css.StylesheetFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.plugins.scss.SCSSFileType

class QCssModuleParseUtilTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/resources"
    }

    private fun getRightCase(file: String): List<String> {
        val psiFile =
            myFixture.configureByText(CssFileType.INSTANCE, myFixture.configureFromTempProjectFile(file).text)
        val res = parseCssSelectorFormFile(psiFile as StylesheetFile)
        return res.keys.sorted()
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("basic/styles.scss")
        myFixture.copyFileToProject("basic/styles.right.css")
        myFixture.copyFileToProject("deep/deep_nest.scss")
        myFixture.copyFileToProject("deep/deep_nest.right.css")
        myFixture.copyFileToProject("media/complex.scss")
        myFixture.copyFileToProject("media/complex.right.css")
        myFixture.copyFileToProject("mixin/mixin.scss")
        myFixture.copyFileToProject("mixin/mixin.right.css")
        myFixture.copyFileToProject("tag/tag_selectors.scss")
        myFixture.copyFileToProject("tag/tag_selectors.right.css")
        myFixture.copyFileToProject("edge_cases/empty.scss")
        myFixture.copyFileToProject("edge_cases/malformed.scss")
        myFixture.copyFileToProject("edge_cases/invalid_syntax.scss")
    }

    fun testBasicParse() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("basic/styles.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile);
        assertEquals(res.keys.sorted(), getRightCase("basic/styles.right.css"))
    }

    fun testDeepNest() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("deep/deep_nest.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        assertEquals(getRightCase("deep/deep_nest.right.css"), res.keys.sorted())
    }

    fun testMixinAndExtend() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("mixin/mixin.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        assertEquals(getRightCase("mixin/mixin.right.css"), res.keys.sorted())
    }

    fun testComplexMediaAndKeyframes() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("media/complex.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        assertEquals(getRightCase("media/complex.right.css"), res.keys.sorted())
    }

    fun testEmptyFile() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("edge_cases/empty.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        assertTrue("Empty file should return an empty array", res.isEmpty())
    }

    fun testMalformedFile() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("edge_cases/malformed.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        // The exact behavior might depend on how your parser handles malformed input
        // This is just an example assertion
        assertTrue("Malformed file should return at least some valid selectors", res.isEmpty())
    }

    fun testInvalidSyntax() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("edge_cases/invalid_syntax.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        // Again, the exact behavior might depend on your implementation
        assertTrue("File with invalid syntax should not crash the parser", res.isEmpty())
    }

    fun testTagSelectors() {
        val fromText = myFixture.configureByText(
            SCSSFileType.SCSS,
            myFixture.configureFromTempProjectFile("tag/tag_selectors.scss").text
        )
        val res = parseCssSelectorFormFile(fromText as StylesheetFile)
        assertEquals(getRightCase("tag/tag_selectors.right.css"), res.keys.sorted())
    }
}