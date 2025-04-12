package com.example.ide.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TagSelectorsCssCompletionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        // 指定测试数据路径为 src/test/resources
        return "src/test/resources"
    }

    fun testTagSelectorsCompletion() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("tag/tag_selectors.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./tag/tag_selectors.right.css";
            s.<caret>;
        """.trimIndent()
        )

        // 触发基本补全
        myFixture.complete(CompletionType.BASIC)

        // 验证补全结果是否正确，包括带单引号的形式
        assertNotNull(myFixture.lookupElementStrings)
        assertNotEmpty(myFixture.lookupElementStrings)
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "'external-link'",
            "highlight",
            "container",
            "text",
            "externalLink"
        )
    }

    fun testTagSelectorsCompletion2() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("tag/tag_selectors.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./tag/tag_selectors.right.css";
            s['<caret>'];
        """.trimIndent()
        )

        // 触发基本补全
        myFixture.complete(CompletionType.BASIC)

        // 验证补全结果是否正确，包括带单引号的形式
        assertNotNull(myFixture.lookupElementStrings)
        assertNotEmpty(myFixture.lookupElementStrings)
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "external-link",
            "highlight",
            "container",
            "text"
        )
    }
}