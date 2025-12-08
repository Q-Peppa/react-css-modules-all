package com.example.ide.completion;

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementDecorator
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
                style.reference?.resolve()?.let { stylesFileImportStatement ->
                    if (stylesFileImportStatement !is ES6ImportedBinding || stylesFileImportStatement.findReferencedElements()
                            .isEmpty()
                    ) return
                    val first = stylesFileImportStatement.findReferencedElements().firstOrNull()
                    first?.let {
                        resultSet.addAllElements(generateLookupElementList(it as StylesheetFile, true).map { element ->
                            // if choose completion with - , auto make to IndexedAccess
                            LookupElementDecorator.withInsertHandler(element) { context, item ->
                                StylesInsertHandler(item.lookupString.contains(SplitChar)).handleInsert(context, item)
                            }
                        })
                    }
                }
            }
        }
    }

    private class StylesInsertHandler(private val needsBracketSyntax: Boolean) : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            if (needsBracketSyntax) {
                val editor = context.editor
                val document = editor.document
                val startOffset = context.startOffset
                val dotPosOffset = startOffset - 1
                val tailOffset = context.tailOffset
                val lookupString = item.lookupString
                document.replaceString(dotPosOffset, tailOffset, "[$lookupString]")
                // move cursor to the end of the inserted text
                // 2 = [ + ]
                editor.caretModel.moveToOffset(dotPosOffset + lookupString.length + 2)
            }
        }
    }

}
