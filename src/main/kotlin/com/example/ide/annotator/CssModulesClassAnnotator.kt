package com.example.ide.annotator

import com.example.ide.psi.CssModulesUnknownClassPsiReference
import com.example.ide.psi.isStyleIndex
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssRuleset
import org.jetbrains.annotations.NotNull


const val MESSAGE = "Selector declarations is Empty"
const val UNKNOWN = "Unknown class name"
class CssModulesClassAnnotator : Annotator {
    private fun resolveUnknownClass(holder: AnnotationHolder, psiElement: JSLiteralExpression) {
        val cssSelectorName = psiElement.stringValue?.trim().orEmpty()
        val reference = psiElement.reference
        if (reference is CssModulesUnknownClassPsiReference) {
            val message = "$UNKNOWN \"$cssSelectorName\""
            holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(psiElement)
                .withFix(SimpleCssSelectorFix(cssSelectorName, reference.stylesheetFile))
                .create()
        }
    }

    private fun resolveEmptyClass(holder: AnnotationHolder, psiElement: JSLiteralExpression) {
        val ruleset = psiElement.reference?.resolve()
        if (ruleset is CssRuleset) {
            val declarations = ruleset.block?.declarations
            if (declarations.isNullOrEmpty()) {
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, MESSAGE)
                    .range(psiElement)
                    .create()
            }
        }
    }

    override fun annotate(@NotNull psiElement: PsiElement, @NotNull holder: AnnotationHolder) {
        if (psiElement is JSLiteralExpression && isStyleIndex(psiElement)) {
            resolveUnknownClass(holder, psiElement)
            resolveEmptyClass(holder, psiElement)
        }
    }
}