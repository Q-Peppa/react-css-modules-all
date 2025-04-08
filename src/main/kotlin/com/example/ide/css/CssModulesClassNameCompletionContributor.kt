package com.example.ide.css

import com.intellij.codeInsight.completion.*
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.util.PathUtil
import com.intellij.util.ProcessingContext

class CssModulesClassNameCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(),
            CssModulesClassNameCompletionContributorProvider()
        )
    }

    private class CssModulesClassNameCompletionContributorProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val position = parameters.position
            // ["$1"] || styles["$1"]
            if (position.parent !is JSLiteralExpression || position.parent.parent !is JSIndexedPropertyAccessExpression) return
            val stylesheetFile = findReferenceStyleFile(position.parent as JSLiteralExpression) ?: return
            // replace path \\ to /
            val shortLocation =
                PathUtil.toSystemIndependentName(SymbolPresentationUtil.getFilePathPresentation(stylesheetFile))
            val allSelector = restoreAllSelector(stylesheetFile)
            val allLookupElement = allSelector.keys.map {
                buildLookupElementHelper(it, allSelector[it]!!, shortLocation)
            }
            resultSet.addAllElements(allLookupElement)
        }
    }
}
