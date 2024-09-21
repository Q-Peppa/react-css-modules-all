package com.example.ide.annotator

import com.example.ide.message.QCssMessageBundle
import com.example.ide.psi.CssModulesUnknownClassPsiReference
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull

class CssModulesClassAnnotator : Annotator {
    override fun annotate(@NotNull psiElement: PsiElement, @NotNull holder: AnnotationHolder) {
        var elementToAnnotate: JSLiteralExpression? = null;
        if (psiElement is JSLiteralExpression) {
            elementToAnnotate = psiElement;
        }
        if (elementToAnnotate == null) return;
        val cssSelectorName = elementToAnnotate.stringValue?.trim() ?: ""
        psiElement.references.let { references ->
            references.forEach {
                if (it is CssModulesUnknownClassPsiReference) {
                    val message = "${QCssMessageBundle.message("UnknownClassName")} \"$cssSelectorName\""
                    holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message)
                        .range(elementToAnnotate)
                        .withFix(SimpleCssSelectorFix(cssSelectorName, it.stylesheetFile))
                        .create()
                }
            }
        }
    }
}