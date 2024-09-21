package com.example.ide.annotator

import com.example.ide.message.QCssMessageBundle
import com.example.ide.psi.CssModulesUnknownClassPsiReference
import com.example.ide.psi.isStyleIndex
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull


class CssModulesClassAnnotator : Annotator {
    override fun annotate(@NotNull psiElement: PsiElement, @NotNull holder: AnnotationHolder) {
        if (psiElement !is JSLiteralExpression) return
        if (!isStyleIndex(psiElement)) return;

        val cssSelectorName = psiElement.stringValue?.trim().orEmpty()
        psiElement.references.forEach {
            if (it is CssModulesUnknownClassPsiReference) {
                val message = "${QCssMessageBundle.message("UnknownClassName")} \"$cssSelectorName\""
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message)
                    .range(psiElement)
                    .withFix(SimpleCssSelectorFix(cssSelectorName, it.stylesheetFile))
                    .create()
            }
        }
    }
}