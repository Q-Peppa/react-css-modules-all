package com.example.ide.css

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CssModulesClassNameCompletionContributorTest : BasePlatformTestCase() {


    fun testCompletionBasic(){
        myFixture.configureByText("kw.css"  ,".app{ color : red}")
        myFixture.configureByText("hello.js"  ,"import cs from './kw.css' ; cs['<caret>']")
        myFixture.completeBasic()
        myFixture.checkResult("""
           import cs from './kw.css';
           cs['app']
        """.trimIndent())
    }
}