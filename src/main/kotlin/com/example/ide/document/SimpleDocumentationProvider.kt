package com.example.ide.document

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssRuleset

class SimpleDocumentationProvider : DocumentationProvider {

    private fun renderDoc(cssRuleset: CssRuleset): String {
        val s = StringBuilder("<pre><code>")
        // appendStyledCodeBlock not compatible in 233.*
        val code = cssRuleset.block?.text?.replace(" ", "")?.replace("{","")?.replace("}","")?.trim() ?: ""
        s.append(code)
        s.append("</code></pre>")
        return  s.toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val origin = super.generateDoc(element, originalElement)
        element ?: return origin
        if (element is CssRuleset) return renderDoc(element)
        return origin
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }
}