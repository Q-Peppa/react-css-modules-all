package com.example.ide.css

import com.example.ide.message.QCssMessageBundle
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.css.StylesheetFile
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull


internal class CssModulesClassNameCompletionContributor : CompletionContributor() {

    private val projectName = QCssMessageBundle.message("projectName")
    private fun buildLookupElement(
        name: String,
        desc: String,
        psiElement: PsiElement?
    ): LookupElement {
        val builder = LookupElementBuilder.create(name)
            .withTailText(desc).withIcon(AllIcons.Xml.Css_class).bold().withCaseSensitivity(true)
        psiElement.let {
            builder.withPsiElement(it)
        }
        return builder
    }

    private fun completionHelper(@NotNull resultSet: CompletionResultSet, @NotNull stylesheetFile: StylesheetFile) {
        if (stylesheetFile.parent == null) return
        val folderName = stylesheetFile.parent?.name
        val fileName = stylesheetFile.name
        val psiElementRefHashMap = QCssModuleParseUtil.parseCssSelectorFormFile(stylesheetFile)
        psiElementRefHashMap.forEach { (name, cssSelectors) ->
            if (cssSelectors.isNotEmpty()) {
                val desc =
                    " (" + folderName + "/" + fileName + ":" + cssSelectors[0].lineNumber + ")_by_" + projectName;
                resultSet.addElement(buildLookupElement(name, desc, cssSelectors[0]))
            }
        }
    }

    private fun addCompletions(@NotNull resultSet: CompletionResultSet, @NotNull stylesheetFile: StylesheetFile) {
        completionHelper(resultSet, stylesheetFile)
    }

    init {

        val provider = object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(
                parameters: CompletionParameters,
                context: ProcessingContext,
                resultSet: CompletionResultSet
            ) {
                val completionElement = parameters.originalPosition ?: parameters.position
                if (completionElement.parent is JSLiteralExpression) {
                    val literalExpression = completionElement.parent as JSLiteralExpression
                    val declaration =
                        QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(literalExpression)

                    declaration?.let {
                        val styleSheetFile = QCssModulesUtil.resolveStyleSheetFile(it)
                        styleSheetFile?.let {
                            addCompletions(resultSet, styleSheetFile)
                        }
                    }
                }
            }
        }
        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            provider
        )
    }
}