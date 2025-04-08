package com.example.ide.psi


import com.example.ide.css.findReferenceStyleFile
import com.example.ide.css.restoreAllSelector
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull


class CssModuleReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is JSLiteralExpression) return PsiReference.EMPTY_ARRAY
        val name = element.stringValue?.trim().orEmpty()
        if (name.isBlank()) return PsiReference.EMPTY_ARRAY
        val styleFile = findReferenceStyleFile(element) ?: return PsiReference.EMPTY_ARRAY
        val map = restoreAllSelector(styleFile)
        return if (map.containsKey(name)) arrayOf(object : PsiReferenceBase<PsiElement>(element) {
            override fun resolve(): PsiElement? = map[name]
        }) else arrayOf(CssModulesUnknownClassPsiReference(element, styleFile))
    }
}

val CLASS_NAME_FILTER = PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(
    FilterPattern(
        object : ElementFilter {
            override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
                return element is JSLiteralExpression
                        && element.parent is JSIndexedPropertyAccessExpression
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

fun isStyleIndex(element: JSLiteralExpression): Boolean = findReferenceStyleFile(element) !== null



