package com.example.ide.psi


import com.example.ide.completion.findReferenceStyleFile
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull


/**
 * Reference provider for styles["className"] syntax
 */
class CssModuleIndexedReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is JSLiteralExpression) return PsiReference.EMPTY_ARRAY
        val name = element.stringValue?.trim().orEmpty()
        if (name.isBlank()) return PsiReference.EMPTY_ARRAY
        val styleFile = findReferenceStyleFile(element) ?: return PsiReference.EMPTY_ARRAY

        // Always return a dynamic reference that resolves fresh each time
        return arrayOf(CssModuleClassReference(element, styleFile, name))
    }
}

/**
 * Reference provider for styles.className syntax
 */
class CssModuleDotReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is JSReferenceExpression) return PsiReference.EMPTY_ARRAY

        // Get the qualifier (the part before the dot, e.g., "styles" in "styles.app")
        val qualifier = element.qualifier
        if (qualifier !is JSReferenceExpression) return PsiReference.EMPTY_ARRAY

        // Resolve the qualifier to check if it's a CSS module import
        val resolved = qualifier.reference?.resolve()
        if (resolved !is ES6ImportedBinding) return PsiReference.EMPTY_ARRAY

        val referencedElements = resolved.findReferencedElements()
        if (referencedElements.isEmpty()) return PsiReference.EMPTY_ARRAY

        val styleFile = referencedElements.first() as? StylesheetFile ?: return PsiReference.EMPTY_ARRAY

        // Get the property name (the part after the dot)
        val propertyName = element.referenceName ?: return PsiReference.EMPTY_ARRAY

        return arrayOf(CssModuleClassReference(element, styleFile, propertyName))
    }
}


// Filter for styles.className syntax
private val DOT_ACCESS_FILTER = PlatformPatterns.psiElement(JSReferenceExpression::class.java).and(
    FilterPattern(
        object : ElementFilter {
            override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {

                return element is JSReferenceExpression
                        && element.reference?.resolve() !== null
                        && (element.reference?.resolve() as ES6ImportedBinding).findReferencedElements()
                    .isNotEmpty()
                        && (element.reference?.resolve() as ES6ImportedBinding).findReferencedElements()
                    .first() is StylesheetFile
            }

            override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
                return JSReferenceExpression::class.java.isAssignableFrom(hintClass!!)
            }
        }
    ))

class CssModulesIndexedStylesVarPsiReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(@NotNull registrar: PsiReferenceRegistrar) {
        // Register provider for styles.className syntax
        registrar.registerReferenceProvider(
            DOT_ACCESS_FILTER, CssModuleDotReferenceProvider()
        )
    }
}

fun isStyleIndex(element: JSLiteralExpression): Boolean = findReferenceStyleFile(element) !== null