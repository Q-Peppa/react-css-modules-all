package com.example.ide.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.css.StylesheetFile

class CssModulesUnknownClassPsiReference(
    element: PsiElement,
    val stylesheetFile: StylesheetFile
) :
    PsiReferenceBase<PsiElement?>(element) {
    override fun resolve(): PsiElement {
        // self reference to prevent JS tooling from reporting unresolved symbol
        return this.element
    }

    override fun getVariants(): Array<Any> {
        return arrayOf(0)
    }
}
