package com.example.ide.psi

import com.example.ide.css.QCssModulesUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Ref
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull

private val LOG = logger<CssModuleReferenceProvider>()

class CssModuleReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is JSLiteralExpression) return PsiReference.EMPTY_ARRAY
        val name = element.stringValue?.trim().orEmpty()
        if (name.isBlank()) return PsiReference.EMPTY_ARRAY

        val cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(element)
            ?: return PsiReference.EMPTY_ARRAY

        val referencedStyleSheet = Ref<StylesheetFile>()
        val cssClass = QCssModulesUtil.getCssClass(cssClassNamesImportOrRequire, name, referencedStyleSheet)
        
        return cssClass?.let {
            arrayOf(object : PsiReferenceBase<PsiElement>(element) {
                override fun resolve(): PsiElement = it
                override fun getVariants(): Array<Any> = arrayOf()
            })
        } ?: referencedStyleSheet.get()?.let {
            try {
                arrayOf(CssModulesUnknownClassPsiReference(element, it))
            } catch (e: Exception) {
                LOG.warn("CssModulesUnknownClassPsiReference cause error $e")
                PsiReference.EMPTY_ARRAY
            }
        } ?: PsiReference.EMPTY_ARRAY
    }
}

val CLASS_NAME_FILTER = PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(
    object : ElementFilter {
        override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            return element is JSLiteralExpression
                    && context != null
                    && context.containingFile is JSFile
                    && isStyleIndex(element)
                    && element.node.firstChildNode?.elementType == JSTokenTypes.STRING_LITERAL
        }

        override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
            return JSLiteralExpression::class.java.isAssignableFrom(hintClass!!)
        }
    }
))

class CssModulesIndexedStylesVarPsiReferenceContributorKt : PsiReferenceContributor() {
    override fun registerReferenceProviders(@NotNull registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            CLASS_NAME_FILTER, CssModuleReferenceProvider()
        )
    }
}

fun isStyleIndex(element: JSLiteralExpression): Boolean = 
    PsiTreeUtil.getParentOfType(element, JSIndexedPropertyAccessExpression::class.java) != null