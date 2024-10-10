package com.example.ide.document

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssRuleset

class SimpleDocumentationProvider : AbstractDocumentationProvider() {
    private fun renderDoc(cssRuleset: CssRuleset): String? {
        val code = StringBuilder()
        if (cssRuleset.block?.declarations?.isEmpty() == true) return null;
        cssRuleset.block?.declarations.let {
            it?.forEach { de -> code.appendLine(de.text) }
        }
        return "<pre><code>${code.toString().trimIndent()}</code></pre>"
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