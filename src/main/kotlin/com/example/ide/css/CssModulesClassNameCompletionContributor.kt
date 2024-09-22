package com.example.ide.css

import com.intellij.codeInsight.completion.*
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class CssModulesClassNameCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), CssModulesClassNameCompletionContributorProvider())
    }
    private  class CssModulesClassNameCompletionContributorProvider: CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val completionElement = parameters.originalPosition ?: parameters.position
            if (completionElement.parent is JSLiteralExpression) {
                val literalExpression = completionElement.parent as JSLiteralExpression
                getCssClassNamesImportOrRequireDeclaration(literalExpression)?.let { declaration ->
                    resolveStyleSheetFile(declaration)?.let { styleSheetFile ->
                        completionHelper(resultSet, styleSheetFile)
                    }
                }
            }
        }
    }
}