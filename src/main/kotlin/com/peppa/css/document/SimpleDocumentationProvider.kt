package com.peppa.css.document

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.QuickDocHighlightingHelper.appendStyledCodeBlock
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssRuleset

private fun String.replaceLast(oldValue: String, newValue: String): String {
    val index = this.lastIndexOf(oldValue)
    return if (index < 0) this else this.substring(0, index) + newValue + this.substring(index + oldValue.length)
}
class SimpleDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? =
        resolveCssRuleset(element, originalElement)
            ?: super.generateDoc(element, originalElement)

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? =
        resolveCssRuleset(element, originalElement)
            ?: super.getQuickNavigateInfo(element, originalElement)

    private fun resolveCssRuleset(element: PsiElement?, context: PsiElement?): String? {
        if (element !is CssRuleset || context == null) return null
        val isStylesAccess = context is JSLiteralExpression
                || context.parent is JSLiteralExpression
                || context is JSReferenceExpression
                || context.parent is JSReferenceExpression
        return if (isStylesAccess) renderDoc(element) else null
    }

    private fun renderDoc(cssRuleset: CssRuleset): String {
        val text = cssRuleset.text.trimIndent().replaceLast("}", "").trim() + "\n}"
        return StringBuilder()
            .appendStyledCodeBlock(cssRuleset.project, CSSLanguage.INSTANCE, code = text)
            .toString()
    }
}