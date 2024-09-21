package com.example.ide.psi

import com.example.ide.css.QCssModulesUtil
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.uml.java.providers.PsiClassParents
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull
import java.util.Objects
import javax.swing.text.Style
import kotlin.math.max

class CssModuleReferenceProvider : PsiReferenceProvider() {
    companion object {
        private const val MIN_TOKEN_LENGTH = 1;
    }

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        assert(element is JSLiteralExpression)
        val js = element as JSLiteralExpression
        println("PsiReferenceProvider invoke name is ${js.stringValue}")
        val name: String = js.stringValue ?: ""
        if (name.isBlank() || name.length != name.trim().length) return PsiReference.EMPTY_ARRAY
        val cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(js)
        if (Objects.isNull(cssClassNamesImportOrRequire)) return PsiReference.EMPTY_ARRAY
        cssClassNamesImportOrRequire?.let {
            val referencedStyleSheet = Ref<StylesheetFile>()
            val cssClass = QCssModulesUtil.getCssClass(it, name.trim(), referencedStyleSheet)
            if (Objects.isNull(cssClass)) {
                referencedStyleSheet.get()?.let {
                    println("not fount ref name is $name")
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
    ReferencesSearch.search(element)
    val father: JSIndexedPropertyAccessExpression =
        PsiTreeUtil.getParentOfType(element, JSIndexedPropertyAccessExpression::class.java)
            ?: return false
    return true;
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

