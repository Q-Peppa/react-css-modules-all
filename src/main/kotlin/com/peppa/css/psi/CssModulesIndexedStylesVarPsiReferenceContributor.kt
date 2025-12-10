package com.peppa.css.psi

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.peppa.css.completion.findReferenceStyleFile

class CssModulesIndexedStylesVarPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(INDEXED_ACCESS_FILTER, IndexedReferenceProvider())
    }

    companion object {
        private val INDEXED_ACCESS_FILTER = PlatformPatterns
            .psiElement(JSLiteralExpression::class.java)
            .withParent(JSIndexedPropertyAccessExpression::class.java)
            .inFile(PlatformPatterns.psiFile(JSFile::class.java))
    }
}

private class IndexedReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val literal = element as? JSLiteralExpression ?: return PsiReference.EMPTY_ARRAY

        if (literal.node.firstChildNode?.elementType != JSTokenTypes.STRING_LITERAL) {
            return PsiReference.EMPTY_ARRAY
        }

        val name = literal.stringValue?.trim()?.takeIf { it.isNotBlank() } ?: return PsiReference.EMPTY_ARRAY
        val styleFile = findReferenceStyleFile(literal) ?: return PsiReference.EMPTY_ARRAY

        return arrayOf(CssModuleClassReference(literal, styleFile, name))
    }
}

fun isStyleIndex(element: JSLiteralExpression): Boolean = findReferenceStyleFile(element) != null