package com.example.ide.css

import com.intellij.codeInsight.completion.*
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.css.StylesheetFile
import com.intellij.util.ProcessingContext

const val SplitChar = "-"
const val DotChar = "."

class CssModulesClassNameCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns
                .psiElement()
                .withLanguage(JavascriptLanguage.INSTANCE)
                // must in '' or ""
                .withParent(JSLiteralExpression::class.java)
                // styles["xxx"]
                .withSuperParent(2, JSIndexedPropertyAccessExpression::class.java),
            CssModulesClassNameCompletionContributorProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns
                .psiElement()
                .withLanguage(JavascriptLanguage.INSTANCE)
                // styles.xxx
                .withParent(JSReferenceExpression::class.java),
            CssModulesClassNameCompletionContributorWithDotProvider()
        )
    }

    // styles["xxx"]
    private class CssModulesClassNameCompletionContributorProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val position = parameters.position
            val stylesheetFile = findReferenceStyleFile(position.parent as JSLiteralExpression) ?: return
            resultSet.addAllElements(generateLookupElementList(stylesheetFile))
        }
    }

    // styles.xxx
    private class CssModulesClassNameCompletionContributorWithDotProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {

            val position = parameters.position
            if (position.prevSibling is PsiElement
                && position.prevSibling.text == DotChar
                && position.prevSibling.prevSibling is JSReferenceExpression
            ) {
                val style = position.prevSibling.prevSibling
                style.reference?.resolve()?.let {
                    if (it !is ES6ImportedBinding || it.findReferencedElements().isEmpty()) return
                    val first = it.findReferencedElements().first()
                    first.let {
                        resultSet.addAllElements(generateLookupElementList(it as StylesheetFile, true).map {
                            // if choose completion with - , auto make to IndexedAccess
                            it.withInsertHandler { context, item ->
                                StylesInsertHandler(item.lookupString.contains(SplitChar)).handleInsert(context, item)
                            }
                        })
                    }
                }
            }
        }
    }
}
