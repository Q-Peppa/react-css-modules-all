package com.peppa.css.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

private const val SPLIT_CHAR = "-"
private const val DOT_CHAR = "."

class CssModulesClassNameCompletionContributor : CompletionContributor() {

    init {
        val jsPattern = PlatformPatterns.psiElement().withLanguage(JavascriptLanguage.INSTANCE)

        extend(
            CompletionType.BASIC,
            jsPattern
                .withParent(JSLiteralExpression::class.java)
                .withSuperParent(2, JSIndexedPropertyAccessExpression::class.java),
            IndexAccessCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            jsPattern.withParent(JSReferenceExpression::class.java),
            DotAccessCompletionProvider()
        )
    }

    private class IndexAccessCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val literal = parameters.position.parent as? JSLiteralExpression ?: return
            val stylesheetFile = findReferenceStyleFile(literal) ?: return
            resultSet.addAllElements(generateLookupElementList(stylesheetFile))
        }
    }

    private class DotAccessCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val position = parameters.position
            val prevSibling = position.prevSibling ?: return
            if (prevSibling.text != DOT_CHAR) return

            val styleRef = prevSibling.prevSibling as? JSReferenceExpression ?: return
            val stylesheetFile = resolveStylesheetFromReference(styleRef) ?: return

            val elements = generateLookupElementList(stylesheetFile, true).map { element ->
                LookupElementDecorator.withInsertHandler(element) { ctx, item ->
                    if (item.lookupString.contains(SPLIT_CHAR)) {
                        convertToBracketSyntax(ctx, item.lookupString)
                    }
                }
            }
            resultSet.addAllElements(elements)
        }

        private fun convertToBracketSyntax(context: InsertionContext, lookupString: String) {
            val document = context.editor.document
            val dotOffset = context.startOffset - 1
            val name = lookupString.removeSurrounding("'").removeSurrounding("\"")
            document.replaceString(dotOffset, context.tailOffset, "['$name']")
            context.editor.caretModel.moveToOffset(context.tailOffset)
        }
    }

}
