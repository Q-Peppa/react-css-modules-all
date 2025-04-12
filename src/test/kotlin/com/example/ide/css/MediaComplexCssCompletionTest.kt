package com.example.ide.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MediaComplexCssCompletionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        // 指定测试数据路径为 src/test/resources
        return "src/test/resources"
    }

    fun testMediaComplexCompletion() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("media/complex.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./media/complex.right.css";
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
            "'mobile-menu'",
            "'tablet-layout'",
            "'animated-element'",
            "'grid-layout'",
            // 驼峰
            "mobileMenu",
            "tabletLayout",
            "animatedElement",
            "gridLayout"
        )
    }

    fun testMediaComplexCompletion2() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("media/complex.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./media/complex.right.css";
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
            "mobile-menu",
            "tablet-layout",
            "animated-element",
            "grid-layout"
        )
    }
}