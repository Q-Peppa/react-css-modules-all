package com.example.ide.css

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns

class CssModulesClassNameCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), CssModulesClassNameCompletionContributorImpl())
    }
}