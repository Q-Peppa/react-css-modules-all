package com.peppa.css.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.concurrent.atomic.AtomicInteger
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.PsiElement

class CssModuleClassReferenceCachingTest : BasePlatformTestCase() {

    fun testProviderCalledEveryTimeWhenCacheDisabled() {
        myFixture.configureByText("a.module.css", ".foo { } .bar { }")
        val file = myFixture.file
        val stylesheet = file as? com.intellij.psi.css.StylesheetFile
            ?: error("expected StylesheetFile")

        val counter = AtomicInteger()
        val provider: (com.intellij.psi.css.StylesheetFile) -> Map<String, SmartPsiElementPointer<PsiElement>> = {
            counter.incrementAndGet(); emptyMap()
        }

        val ref = CssModuleClassReference(myFixture.file, stylesheet, "foo", selectorProvider = provider, enableCache = false)

        repeat(10) { ref.resolve() }
        assertEquals(10, counter.get())
    }

    fun testProviderCalledOnceWhenCacheEnabled() {
        myFixture.configureByText("a.module.css", ".foo { } .bar { }")
        val file = myFixture.file
        val stylesheet = file as? com.intellij.psi.css.StylesheetFile
            ?: error("expected StylesheetFile")

        val counter = AtomicInteger()
        val provider: (com.intellij.psi.css.StylesheetFile) -> Map<String, SmartPsiElementPointer<PsiElement>> = {
            counter.incrementAndGet(); emptyMap()
        }

        val ref = CssModuleClassReference(myFixture.file, stylesheet, "foo", selectorProvider = provider, enableCache = true)

        repeat(10) { ref.resolve() }
        assertEquals(1, counter.get())
    }

    fun testCacheInvalidatedWhenModificationStampChanges() {
        myFixture.configureByText("a.module.css", ".foo { } .bar { }")
        val file = myFixture.file
        val stylesheet = file as? com.intellij.psi.css.StylesheetFile
            ?: error("expected StylesheetFile")

        val counter = AtomicInteger()
        val provider: (com.intellij.psi.css.StylesheetFile) -> Map<String, SmartPsiElementPointer<PsiElement>> = {
            counter.incrementAndGet(); emptyMap()
        }

        val ref = CssModuleClassReference(myFixture.file, stylesheet, "foo", selectorProvider = provider, enableCache = true)

        // 首次解析
        ref.resolve()
        val firstCount = counter.get()

        // 修改文件触发 stamp 变更
        myFixture.configureByText("a.module.css", ".foo { color: red } .bar { }")

        // 再次解析应触发 provider
        ref.resolve()
        val secondCount = counter.get()

        assertTrue(firstCount >= 1)
        assertTrue(secondCount > firstCount)
    }
}
