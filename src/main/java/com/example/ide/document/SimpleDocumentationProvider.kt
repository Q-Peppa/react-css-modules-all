package com.example.ide.document

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssSelector
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

class SimpleDocumentationProvider : AbstractDocumentationProvider() {

    private fun renderDoc(cssSelector: CssSelector, name: String): String? {
        var selectorName = name;
        if (selectorName.isBlank()) selectorName = cssSelector.text
        val content = StringBuilder()
        content.append(DocumentationMarkup.DEFINITION_START)
        content.append(". $selectorName")
        content.append(DocumentationMarkup.DEFINITION_END)
        content.append("<hr/>")
        content.append(DocumentationMarkup.SECTION_START)
        val ruleset = cssSelector.ruleset
        ruleset?.block.let {
            it?.declarations?.forEach { declaration ->
                content.append(HtmlChunk.tag("pre").addText(declaration?.text?.trim() ?: ""))
            }
        }
        content.append(DocumentationMarkup.SECTION_END)
        content.append("<hr/>")
        return content.trim().toString()
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val origin = super.generateDoc(element, originalElement)
        if (Objects.isNull(element) || Objects.isNull(originalElement)) return origin;
        val expression = PsiTreeUtil.findChildOfType(element, JSLiteralExpression::class.java)
        val resolve = expression?.reference?.resolve()
        if (resolve != null && resolve is CssSelector) {
            return renderDoc(resolve, expression.stringValue ?: "")
        }
        return origin
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }
}