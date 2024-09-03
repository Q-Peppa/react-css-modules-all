package com.example.css;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.CssSelectorImpl;
import com.intellij.psi.css.impl.CssSelectorListImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


final class CssModulesClassNameCompletionContributor extends CompletionContributor {


    private static LookupElement buildLookupElement(
            @NotNull String name,
            @NotNull String desc,
            @Nullable PsiElement psiElement
    ) {
        LookupElementBuilder builder = LookupElementBuilder.create(name).withTailText(desc).withIcon(AllIcons.Xml.Css_class).bold().withCaseSensitivity(true);
        if (psiElement != null) {
            builder = builder.withPsiElement(psiElement);
        }
        return builder;
    }

    CssModulesClassNameCompletionContributor() {

        var provider = new CompletionProvider() {


            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                if (completionElement.getParent() instanceof JSLiteralExpression) {
                    JSLiteralExpression literalExpression = (JSLiteralExpression) completionElement.getParent();
                    final PsiElement cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(literalExpression);
                    if (cssClassNamesImportOrRequire != null) {
                        final StylesheetFile stylesheetFile = QCssModulesUtil.resolveStyleSheetFile(cssClassNamesImportOrRequire);
                        if (stylesheetFile != null) {
                            addCompletions(completionResultSet, stylesheetFile);
                        }
                    }
                }
            }

            private void addCompletions(@NotNull CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
                if (stylesheetFile.getParent() == null) return;
                final String folderName = stylesheetFile.getParent().getName();
                final String fileName = stylesheetFile.getName();

                /**
                 * core 方法 收集全部的class name
                 */
                QCssModulesUtil.preLoad(stylesheetFile);

                for (String already : QCssModulesUtil.alreadyProcess) {
                    CssSelector[] selectors = QCssModulesUtil.psiElementRefHashMap.get(already);
                    final String name = StringUtils.trim(already.replaceFirst(".", ""));
                    if (name.contains(":global")) continue;
                    if (selectors == null || selectors.length == 0) continue;
                    final String desc = " (" + folderName + "/" + fileName + ":" + selectors[0].getLineNumber() + ")_by_css_module_all";
                    resultSet.addElement(buildLookupElement(name, desc, selectors[0]));
                }

                /**
                 * 可能有些CssName 没有收集到
                 */
                for (var cssClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass.class)) {
                    String name = cssClass.getText();
                    if (QCssModulesUtil.psiElementRefHashMap.containsKey(name)) continue;
                    final String cssName = StringUtils.trim(name.replaceFirst(".", ""));
                    CssSelectorList f = (CssSelectorList) PsiTreeUtil.findFirstParent(cssClass, e -> e instanceof CssSelectorList);

                    if (QCssModulesUtil.isInTheGlobal(cssClass)) continue;
                    if (f == null) continue;
                    QCssModulesUtil.psiElementRefHashMap.put(StringUtils.trim(name), f.getSelectors());
                    final String desc = " (" + folderName + "/" + fileName + ":" + cssClass.getLineNumber() + ")_by_css_module_all";
                    resultSet.addElement(
                            buildLookupElement(cssName, desc, cssClass)

                    );
                }

            }


        };


        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }
}