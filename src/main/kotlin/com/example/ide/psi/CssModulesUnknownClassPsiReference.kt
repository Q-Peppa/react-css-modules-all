package com.example.ide.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.css.StylesheetFile

class CssModulesUnknownClassPsiReference(
    element: PsiElement,
    val stylesheetFile: StylesheetFile
) :
    PsiReferenceBase<PsiElement?>(element) {
    override fun resolve(): PsiElement = element
}
