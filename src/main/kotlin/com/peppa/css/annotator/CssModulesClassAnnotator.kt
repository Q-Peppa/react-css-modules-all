package com.peppa.css.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.peppa.css.completion.resolveStylesheetFromReference
import com.peppa.css.psi.CssModuleClassReference
import com.peppa.css.psi.isStyleIndex

private const val MESSAGE_UNKNOWN = "Unknown class name"

class CssModulesClassAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        when (psiElement) {
            is JSLiteralExpression if isStyleIndex(psiElement) ->
                annotateStyleIndex(psiElement, holder)

            is JSReferenceExpression -> annotateUnresolvedReference(psiElement, holder)
        }
    }

    private fun annotateStyleIndex(element: JSLiteralExpression, holder: AnnotationHolder) {
        val reference = element.reference as? CssModuleClassReference ?: return
        val className = element.stringValue?.trim().orEmpty()

        when {
            reference.isUnresolved() -> holder.newAnnotation(HighlightSeverity.WARNING, "$MESSAGE_UNKNOWN \"$className\"")
                .range(element)
                .withFix(SimpleCssSelectorFix(className, reference.stylesheetFile))
                .create()
        }
    }

    private fun annotateUnresolvedReference(element: JSReferenceExpression, holder: AnnotationHolder) {
        if (element.reference?.resolve() != null) return

        val styleFile = resolveStylesheetFromReference(element) ?: return
        val className = element.lastChild.text

        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "$MESSAGE_UNKNOWN \"$className\"")
            .range(element.lastChild)
            .withFix(SimpleCssSelectorFix(className, styleFile))
            .create()
    }
}