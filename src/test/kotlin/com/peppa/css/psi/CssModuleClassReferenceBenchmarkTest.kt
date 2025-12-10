package com.peppa.css.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.system.measureNanoTime

class CssModuleClassReferenceBenchmarkTest : BasePlatformTestCase() {

    fun testBenchmarkCacheVsNoCache() {
        myFixture.configureByText("a.module.css", (1..100).joinToString("\n") { ".c$it { color: black }" })
        val file = myFixture.file
        val stylesheet = file as? com.intellij.psi.css.StylesheetFile
            ?: error("expected StylesheetFile")

        val refNoCache = CssModuleClassReference(myFixture.file, stylesheet, "c1", enableCache = false)
        val refCache = CssModuleClassReference(myFixture.file, stylesheet, "c1", enableCache = true)

        val runs = 5000

        val timeNoCache = measureNanoTime {
            repeat(runs) { refNoCache.resolve() }
        }

        val timeCache = measureNanoTime {
            repeat(runs) { refCache.resolve() }
        }

        println("no-cache: $timeNoCache ns, cache: $timeCache ns, ratio: ${timeNoCache.toDouble()/timeCache}")

        // 主要断言还是 provider 调用次数应有所差异；时间仅作参考
        assertTrue(timeCache < timeNoCache)
    }
}

