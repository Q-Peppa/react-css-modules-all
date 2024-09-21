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
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull
import java.util.*


private val LOG = logger<CssModuleReferenceProvider>()

class CssModuleReferenceProvider : PsiReferenceProvider() {

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
        cssClassNamesImportOrRequire?.let {
            val referencedStyleSheet = Ref<StylesheetFile>()
            val cssClass = QCssModulesUtil.getCssClass(it, name.trim(), referencedStyleSheet)
            if (Objects.isNull(cssClass)) {
                if (referencedStyleSheet.get() != null ) {
                    try {
                        val r =  PsiReference.ARRAY_FACTORY.create(1)
                        r[0] = CssModulesUnknownClassPsiReference(element, referencedStyleSheet.get())
                        return  r;
                    }catch (e :Exception) {
                        LOG.warn("CssModulesUnknownClassPsiReference cause error $e")
                    }

                }
            }
            return arrayOf(object : PsiReferenceBase<PsiElement>(element) {
                override fun resolve(): PsiElement? {
                    return cssClass
                }
                override fun getVariants(): Array<Any> {
                    return arrayOf(0)
                }
            })
        }
        return PsiReference.EMPTY_ARRAY;
    }

}

class CssModulesIndexedStylesVarPsiReferenceContributorKt : PsiReferenceContributor() {
    override fun registerReferenceProviders(@NotNull registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            CLASS_NAME_FILTER, CssModuleReferenceProvider()
        )
    }
}

fun isStyleIndex(element:JSLiteralExpression):Boolean{
    return PsiTreeUtil.getParentOfType(element, JSIndexedPropertyAccessExpression::class.java) != null
}
val CLASS_NAME_FILTER = PlatformPatterns.psiElement(JSLiteralExpression::class.java).and(FilterPattern(
    object : ElementFilter {
        override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            return if (element is JSLiteralExpression
                && context != null
                && context.containingFile is JSFile
                && isStyleIndex(element)) {
                val value = element.node.firstChildNode
                Objects.nonNull(value) && value.elementType == JSTokenTypes.STRING_LITERAL
            } else false
        }

        override fun isClassAcceptable(hintClass: Class<*>?): Boolean {
            return JSLiteralExpression::class.java.isAssignableFrom(hintClass!!)
        }
    }
))

