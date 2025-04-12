package com.example.ide.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MixinCssCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/resources"

    fun testMixinCssCompletion() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("mixin/mixin.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./mixin/mixin.right.css";
            s.<caret>;
        """.trimIndent()
        )

        // 触发基本补全
        myFixture.complete(CompletionType.BASIC)

        // 验证补全结果是否正确，包括驼峰形式
        assertNotNull(myFixture.lookupElementStrings)
        assertNotEmpty(myFixture.lookupElementStrings)
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "'secondary-button'",
            "'primary-button'",
            "secondaryButton", // 驼峰形式
            "primaryButton"    // 驼峰形式
        )
    }

    fun testMixinCssCompletion2() {
        // 将 CSS 文件复制到项目中
        myFixture.copyFileToProject("mixin/mixin.right.css")

        // 配置测试文件
        myFixture.configureByText(
            "test.js", """
            import s from "./mixin/mixin.right.css";
            s['<caret>'];
        """.trimIndent()
        )

        // 触发基本补全
        myFixture.complete(CompletionType.BASIC)

        // 验证补全结果是否正确，包括驼峰形式
        assertNotNull(myFixture.lookupElementStrings)
        assertNotEmpty(myFixture.lookupElementStrings)
        assertSameElements(
            myFixture.lookupElementStrings!!,
            "secondary-button",
            "primary-button",
        )
    }

}