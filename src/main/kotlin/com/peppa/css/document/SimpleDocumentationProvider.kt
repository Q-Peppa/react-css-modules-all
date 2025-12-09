package com.peppa.css.document

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.QuickDocHighlightingHelper.appendStyledCodeBlock
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssRuleset

private fun String.replaceLast(target: String, replacement: String): String {
    val index = this.lastIndexOf(target)
    return if (index != -1) {
        this.substring(0, index) + replacement + this.substring(index + target.length)
    } else {
        this // 如果没有找到匹配项，返回原字符串
    }
}

class SimpleDocumentationProvider : AbstractDocumentationProvider() {
    private fun renderDoc(cssRuleset: CssRuleset): String {
        val text = cssRuleset.text.trimIndent().replaceLast("}", "").trim() + "\n}"
        return StringBuilder()
            .appendStyledCodeBlock(cssRuleset.project, CSSLanguage.INSTANCE, code = text)
            .toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null || originalElement == null) return null

        val origin = super.generateDoc(element, originalElement)
        // Check if the element is a CssRuleset and the originalElement is a JSLiteralExpression
        if (element is CssRuleset && originalElement.parent is JSLiteralExpression) {
            return renderDoc(element)
        }
        return origin
    }

    /**
     * override getQuickNavigateInfo when ctrl/cmd + mouse left hover show css ruleset
     */
    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element is CssRuleset && originalElement is JSLiteralExpression) {
            return renderDoc(element)
        }
        return super.getQuickNavigateInfo(element, originalElement)
    }
}