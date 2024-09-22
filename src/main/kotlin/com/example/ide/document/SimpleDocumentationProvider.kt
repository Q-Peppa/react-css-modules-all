package com.example.ide.document

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssSelector
import com.intellij.psi.util.PsiTreeUtil

class SimpleDocumentationProvider : DocumentationProvider {

    private fun renderDoc(cssSelector: CssSelector): String {
        val s = StringBuilder("<pre><code>")
        // appendStyledCodeBlock not compatible in 233.*
        val code =cssSelector.ruleset?.block?.text?.replace(" ", "")?.replace("{","")?.replace("}","")?.trim() ?: ""
        s.append(code)
        s.append("</code></pre>")
        return  s.toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val origin = super.generateDoc(element, originalElement)
        element ?: return origin

        if (element.parent is CssSelector) return renderDoc(element.parent as CssSelector)

        return when (element) {
            is CssSelector -> renderDoc(element)
            else -> {

                val expression = PsiTreeUtil.findChildOfType(element, JSLiteralExpression::class.java)
                val resolve = expression?.reference?.resolve()
                if (resolve?.parent is CssSelector) return renderDoc(resolve.parent as CssSelector)
                if (resolve is CssSelector) renderDoc(resolve) else origin
            }
        }
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }
}