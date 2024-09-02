package com.example.css;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;


final class CssModulesClassNameCompletionContributor extends CompletionContributor {


    CssModulesClassNameCompletionContributor(){

       var provider = new CompletionProvider() {
           private static final HashSet<String> alreadyProcess = new HashSet<>();
           private static final HashMap<String, PsiElement> psiElementHashMap = new HashMap();

           @Override
           protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

               final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
//               for(var s :completionResultSet.consume;) {}
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

           private void addCompletions(
                   @NotNull CompletionResultSet resultSet,
                   @NotNull StylesheetFile stylesheetFile
           ) {


               for (CssRuleset cssClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssRuleset.class)) {
                   final String name = cssClass.getPresentableText();

                   String clazzName = parseClass(cssClass);
                   if (clazzName!=null) {
                       alreadyProcess.add(clazzName);
                       psiElementHashMap.put(clazzName ,cssClass );
                   }
               }
               for(String already : alreadyProcess){
                   CssRuleset css = (CssRuleset)psiElementHashMap.get(already);
                   CssSelector[] cssSelectorList = Objects.requireNonNull(css.getSelectorList()).getSelectors();
                   resultSet.addElement(
                           LookupElementBuilder.create(already.replaceFirst(".", ""))
                                   .withPsiElement(css)
                                   .withIcon(AllIcons.Xml.Css_class)
                                   .withTailText("( line is :"+ cssSelectorList[0].getLineNumber() + ")")

                   );
               }

           }

           private String parseClass(CssRuleset cssClass) {
               String name = cssClass.getPresentableText();
               if (name.startsWith(".")) {
                   PsiElement psiElement = cssClass;
                   while (!(psiElement.getParent() instanceof CssStylesheet)) {
                       PsiElement parent = psiElement.getParent();
                       if (parent instanceof CssRuleset) {
                           CssRuleset parentRule = (CssRuleset) parent;
                           String presentableText = parentRule.getPresentableText();
                           if(presentableText.startsWith(":global")) {
                               return null;
                           }
                       }
                       psiElement = parent;
                   }
                   return name;
               }
               if(name.startsWith(":global")) {
                   return null;
               }
               name = name.replaceFirst("&","");
               PsiElement psiElement = cssClass;
               while (!(psiElement.getParent() instanceof CssStylesheet)) {
                   PsiElement parent = psiElement.getParent();
                   if (parent instanceof CssRuleset) {
                       CssRuleset parentRule = (CssRuleset) parent;
                       String presentableText = parentRule.getPresentableText();
                       if(presentableText.startsWith(":global")) {
                           return null;
                       } else if (presentableText.startsWith(".")) {
                           return presentableText+name;
                       } else if (presentableText.startsWith("&")) {
                           name = presentableText.replaceFirst("&","") + name;
                       }
                   }
                   psiElement = parent;
               }
               return null;
           }


       };




       extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

   }
}
