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

        var provider = new CompletionProvider<>() {


            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                if (completionElement.getParent() instanceof JSLiteralExpression literalExpression) {
                    final PsiElement cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration(literalExpression);
                    if (cssClassNamesImportOrRequire != null) {
                        final StylesheetFile stylesheetFile = QCssModulesUtil.resolveStyleSheetFile(cssClassNamesImportOrRequire);
                        if (stylesheetFile != null) {
                            addCompletions(completionResultSet, stylesheetFile);
                        }
                    }
                }
            }


            private void resolveNormalClassName(@NotNull CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
                if (stylesheetFile.getParent() == null) return;
                final String folderName = stylesheetFile.getParent().getName();
                final String fileName = stylesheetFile.getName();
                for (CssClass aClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass.class)) {
                    if (QCssModulesUtil.isInTheGlobal(aClass)) continue;
                    String name = aClass.getText();

                    if (QCssModulesUtil.alreadyProcess.contains(name)) continue;

                    QCssModulesUtil.alreadyProcess.add(name);
                    CssRuleset ruleset = aClass.getRuleset();
                    if (ruleset != null) {
                        CssSelector[] selectors = ruleset.getSelectors();
                        final String desc = " (" + folderName + "/" + fileName + ":" + selectors[0].getLineNumber() + ")_by_css_module_all";
                        QCssModulesUtil.psiElementRefHashMap.put(name, selectors);
                        final String cssName = StringUtils.trim(name.replaceFirst(".", ""));
                        resultSet.addElement(buildLookupElement(cssName, desc, selectors[0]));
                    }

                }
            }

            private void resolveScssParentSelector(@NotNull CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
                if (stylesheetFile.getParent() == null) return;
                final String folderName = stylesheetFile.getParent().getName();
                final String fileName = stylesheetFile.getName();
                for (CssSimpleSelector simpleSelector : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssSimpleSelector.class)) {
                    String text = StringUtils.trim(simpleSelector.getText());
                    if (!text.startsWith("&") || QCssModulesUtil.isInTheGlobal(simpleSelector)) continue;
                    /**
                     * &-foo 到 最顶层的全路径
                     */
                    final ArrayList<String> path = new ArrayList<>();

                    PsiTreeUtil.findFirstParent(simpleSelector, parent -> {
                        if (parent instanceof CssRuleset) {
                            path.add(((CssRuleset) parent).getPresentableText());
                            return ((CssRuleset) parent).getPresentableText().startsWith(".");
                        }
                        return false;
                    });
                    CssSelector selectors = (CssSelector) simpleSelector.getParent();
                    // 倒转css class 的顺序
                    ArrayList<String> cssList = QScssUtil.getOriginCss(path.reversed());
                    final String desc = " (" + folderName + "/" + fileName + ":" + selectors.getLineNumber() + ")_by_css_module_all";
                    for (String name : cssList) {
                        QCssModulesUtil.psiElementRefHashMap.put(name, new CssSelector[]{selectors});
                        final String cssName = StringUtils.trim(name.replaceFirst(".", ""));
                        resultSet.addElement(buildLookupElement(cssName, desc, selectors));
                    }

                }
            }

            private void addCompletions(@NotNull CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
                QCssModulesUtil.initContainer(); // init all classname tracker
                resolveNormalClassName(resultSet, stylesheetFile); // resolve startWith DOT class ,
                resolveScssParentSelector(resultSet, stylesheetFile);
            }
        };

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }
}