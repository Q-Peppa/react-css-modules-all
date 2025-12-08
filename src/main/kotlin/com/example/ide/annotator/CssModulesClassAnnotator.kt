package com.example.ide.annotator

import com.example.ide.completion.resolveStylesheetFromReference
import com.example.ide.psi.CssModuleClassReference
import com.example.ide.psi.isStyleIndex
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssRuleset
import org.jetbrains.annotations.NotNull


const val MESSAGE = "Selector declarations is Empty"
const val UNKNOWN = "Unknown class name"

class CssModulesClassAnnotator : Annotator {
    private fun resolveUnknownClass(holder: AnnotationHolder, psiElement: JSLiteralExpression) {
        val cssSelectorName = psiElement.stringValue?.trim().orEmpty()
        val reference = psiElement.reference

        // Check if the reference is unresolved (CSS class doesn't exist)
        if (reference is CssModuleClassReference && reference.isUnresolved()) {
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
        if (psiElement is JSReferenceExpression) {
            val styleFile = resolveStylesheetFromReference(psiElement) ?: return
            if (psiElement.reference?.resolve() !is CssClass) {
                holder.newAnnotation(
                    HighlightSeverity.WEAK_WARNING,
                    "$UNKNOWN \"${psiElement.lastChild.text}\""
                )
                    .range(psiElement.lastChild)
                    .withFix(SimpleCssSelectorFix(psiElement.lastChild.text, styleFile))
                    .create()
            }
        }
    }
}