package com.example.ide.css;

import com.intellij.codeInsight.completion.*;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.CssSimpleSelectorImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;


final class CssModulesClassNameCompletionContributor extends CompletionContributor {


    private final static String ProjectName = QCssMessage.message("projectName");

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


    public static void resolveNormalClassName(@Nullable CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
        if (stylesheetFile.getParent() == null) return;
        final String folderName = stylesheetFile.getParent().getName();
        final String fileName = stylesheetFile.getName();
        for (CssClass aClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass.class)) {
            if (QCssModulesUtil.isInTheGlobal(aClass)) continue;
            String name = aClass.getText();

            if (QCssModulesUtil.psiElementRefHashMap.containsKey(name)) continue;

            CssRuleset ruleset = aClass.getRuleset();
            if (ruleset != null) {
                CssSelector[] selectors = ruleset.getSelectors();
                final String desc = " (" + folderName + "/" + fileName + ":" + selectors[0].getLineNumber() + ")_by_" + ProjectName;
                QCssModulesUtil.psiElementRefHashMap.put(name, selectors);
                final String cssName = StringUtils.trim(name.replaceFirst(".", ""));
                if (resultSet != null) {
                    resultSet.addElement(buildLookupElement(cssName, desc, selectors[0]));
                }
            }

        }
    }

    public static void resolveScssParentSelector(@Nullable CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
        if (stylesheetFile.getParent() == null) return;
        final String folderName = stylesheetFile.getParent().getName();
        final String fileName = stylesheetFile.getName();
        for (CssSimpleSelectorImpl simpleSelector : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssSimpleSelectorImpl.class)) {
            String text = StringUtils.trim(simpleSelector.getPresentableText());
            if (text.length() <= 1) continue;  //  "????"
            if (!text.contains("&") || QCssModulesUtil.isInTheGlobal(simpleSelector)) continue;

            text = "&" + StringUtils.substringAfter(text, "&");
            // &-foo 到 最顶层的全路径
            final ArrayList<String> path = new ArrayList<>();
            PsiTreeUtil.findFirstParent(simpleSelector, parent -> {
                if (parent instanceof CssRuleset css) {
                    final String cellName = StringUtils.trim(css.getPresentableText());
                    path.add(cellName);
                    return cellName.startsWith(".") && !cellName.contains("&");
                }
                return false;
            });
            CssSelector selectors = (CssSelector) simpleSelector.getParent();
            if (path.size() <= 1) continue;

            path.set(0, text); // 收集数据的起始一定是一个 & 开头的选择器
            Collections.reverse(path); // 收集的时候是从下往上, 所以这里应该翻转一下顺序
            ArrayList<String> cssList = QScssUtil.getOriginCss(path);
            final String desc = " (" + folderName + "/" + fileName + ":" + selectors.getLineNumber() + ")_by_" + ProjectName;
            for (String name : cssList) {
                if (String.valueOf(name).contains("&")) continue;
                if (String.valueOf(name).contains(":")) {
                    name = ArrayUtil.getLastElement(name.split(":")); // 移除掉生成好的伪元素 or 伪类
                }
                if (QCssModulesUtil.psiElementRefHashMap.containsKey(name)) {
                    continue;
                }
                QCssModulesUtil.psiElementRefHashMap.put(name, new CssSelector[]{selectors});

                final String cssName = StringUtils.trim(name.replaceFirst(".", ""));
                if (resultSet != null) {
                    resultSet.addElement(buildLookupElement(cssName, desc, selectors));
                }

            }

        }
    }

    /**
     * @param resultSet      if CompletionResultSet not null , will add completion
     * @param stylesheetFile (css or scss or less) file
     */
    public static void completionHelper(@Nullable CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
        resolveNormalClassName(resultSet, stylesheetFile); // resolve startWith DOT class
        final String fileType = stylesheetFile.getName();
        Pattern cssFile = Pattern.compile("\\.css$", Pattern.CASE_INSENSITIVE);
        if (cssFile.matcher(fileType).find()) {
            return;
        }
        resolveScssParentSelector(resultSet, stylesheetFile);
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

            private void addCompletions(@NotNull CompletionResultSet resultSet, @NotNull StylesheetFile stylesheetFile) {
                QCssModulesUtil.initContainer();
                completionHelper(resultSet, stylesheetFile);
            }
        };

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }
}