package com.peppa.css.document

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.QuickDocHighlightingHelper.appendStyledCodeBlock
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssRuleset

class SimpleDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        context: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        if (context == null) return null
        val parent = context.parent ?: context
        for (ref in parent.references) {
            val resolved = ref.resolve()
            if (resolved is CssRuleset) return resolved
        }
        for (ref in context.references) {
            val resolved = ref.resolve()
            if (resolved is CssRuleset) return resolved
        }
        return null
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is CssRuleset || !isValidContext(originalElement)) return null
        return renderDoc(element)
    }

    private fun isValidContext(context: PsiElement?): Boolean =
        context is JSLiteralExpression
                || context?.parent is JSLiteralExpression
                || context is JSReferenceExpression
                || context?.parent is JSReferenceExpression

    private fun renderDoc(cssRuleset: CssRuleset): String {
        val text = cssRuleset.text.trimIndent()
        return StringBuilder()
            .appendStyledCodeBlock(cssRuleset.project, CSSLanguage.INSTANCE, code = text)
            .toString()
    }
}
