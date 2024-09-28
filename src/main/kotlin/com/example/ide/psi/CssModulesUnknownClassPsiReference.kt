package com.example.ide.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.css.StylesheetFile

class CssModulesUnknownClassPsiReference(
    element: PsiElement,
    val stylesheetFile: StylesheetFile
) :
    PsiReferenceBase<PsiElement?>(element) {
    // pointer themselves
    override fun resolve(): PsiElement = this.element
    override fun getVariants(): Array<Any> = arrayOf(0)
}
