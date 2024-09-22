package com.example.ide.document

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.documentation.QuickDocHighlightingHelper.appendStyledCodeBlock
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssSelector
import com.intellij.psi.util.PsiTreeUtil

class SimpleDocumentationProvider : DocumentationProvider {

    private fun renderDoc(cssSelector: CssSelector): String {
        return StringBuilder().apply {
            appendStyledCodeBlock(cssSelector.project, CSSLanguage.INSTANCE, cssSelector.ruleset?.text?.trim() ?: "")
        }.toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val origin = super.generateDoc(element, originalElement)
        element ?: return origin

        return when (element) {
            is CssSelector -> renderDoc(element)
            else -> {
                val expression = PsiTreeUtil.findChildOfType(element, JSLiteralExpression::class.java)
                val resolve = expression?.reference?.resolve()
                if (resolve is CssSelector) renderDoc(resolve) else origin
            }
        }
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }
}