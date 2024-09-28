package com.example.ide.document

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.css.CssClass

class SimpleDocumentationProvider : AbstractDocumentationProvider()  {


    private fun renderDoc(cssRuleset: CssClass): String {
        val s = StringBuilder("<pre><code>")
        // appendStyledCodeBlock not compatible in 233.*
        val code = cssRuleset.text
        s.append(code)
        s.append("</code></pre>")
        return  s.toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null || originalElement == null) return null;
        println(element?.text)
        val origin = super.generateDoc(element, originalElement)
        element ?: return origin
        if (element is CssClass) return renderDoc(element)
        return origin
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val file = element?.containingFile
        return file?.name
    }

}