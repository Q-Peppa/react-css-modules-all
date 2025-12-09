package com.peppa.css.psi


import com.peppa.css.completion.findReferenceStyleFile
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull


/**
 * Reference provider for styles["className"] syntax.
 * All validation logic is centralized here to avoid duplicate PSI resolution.
 */
class CssModuleIndexedReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is JSLiteralExpression) return PsiReference.EMPTY_ARRAY

        // Fast check: must be a string literal token
        if (element.node.firstChildNode?.elementType != JSTokenTypes.STRING_LITERAL) {
            return PsiReference.EMPTY_ARRAY
        }

        // Fast check: must be inside indexed property access (styles["xxx"])
        if (element.parent !is JSIndexedPropertyAccessExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        val name = element.stringValue?.trim().orEmpty()
        if (name.isBlank()) return PsiReference.EMPTY_ARRAY

        // Expensive check: resolve to style file (done only once here)
        val styleFile = findReferenceStyleFile(element) ?: return PsiReference.EMPTY_ARRAY

        // Always return a dynamic reference that resolves fresh each time
        return arrayOf(CssModuleClassReference(element, styleFile, name))
    }
}

/**
 * Simplified filter - only does basic type matching.
 * Expensive PSI resolution is deferred to the provider.
 */
private val INDEXED_ACCESS_FILTER = PlatformPatterns
    .psiElement(JSLiteralExpression::class.java)
    .withParent(JSIndexedPropertyAccessExpression::class.java)
    .inFile(PlatformPatterns.psiFile(JSFile::class.java))


class CssModulesIndexedStylesVarPsiReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(@NotNull registrar: PsiReferenceRegistrar) {
        // Register provider for styles["className"] syntax
        registrar.registerReferenceProvider(
            INDEXED_ACCESS_FILTER, CssModuleIndexedReferenceProvider()
        )
    }
}

/**
 * Check if the element is a style index expression (styles["className"]).
 * This function is used by other parts of the codebase (e.g., annotator).
 */
fun isStyleIndex(element: JSLiteralExpression): Boolean = findReferenceStyleFile(element) != null