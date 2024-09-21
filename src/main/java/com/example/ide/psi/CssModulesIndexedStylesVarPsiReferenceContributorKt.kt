package com.example.ide.psi

import com.example.ide.css.QCssModulesUtil
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.StylesheetFile
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull
import java.util.Objects
import kotlin.math.max

class CssModuleReferenceProvider : PsiReferenceProvider() {
    companion object {
        private const val DOT = ".";
        private const val MIN_TOKEN_LENGTH = 1;
    }

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        assert(element is JSLiteralExpression)
        val js = element as JSLiteralExpression
        val name: String = js.stringValue ?: ""
        if (name.isBlank() || name.length != name.trim().length) return PsiReference.EMPTY_ARRAY
        val cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(js)
        if (Objects.isNull(cssClassNamesImportOrRequire)) return PsiReference.EMPTY_ARRAY
        cssClassNamesImportOrRequire.let {
            val literalClass = DOT + name.trim()
            val referencedStyleSheet = Ref<StylesheetFile>()
            val cssClass = QCssModulesUtil.getCssClass(it, literalClass, referencedStyleSheet)
            if (Objects.isNull(cssClass)) {
                referencedStyleSheet.get().let {
                    val textRange = TextRange.from(1, max(element.textLength - 2, MIN_TOKEN_LENGTH))
                    return arrayOf(
                        CssModulesUnknownClassPsiReference(
                            element,
                            textRange,
                            referencedStyleSheet.get()
                        )
                    )
                }
            }
            return arrayOf(object : PsiReferenceBase<PsiElement>(element) {
                override fun resolve(): PsiElement? {
                    return cssClass
                }

                override fun getVariants(): Array<Any> {
                    return arrayOf(0)
                }
            }
            )
        }
    }

}

class CssModulesIndexedStylesVarPsiReferenceContributorKt : PsiReferenceContributor() {
    override fun registerReferenceProviders(@NotNull registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JSLiteralExpression::class.java), CssModuleReferenceProvider()
        )
    }
}