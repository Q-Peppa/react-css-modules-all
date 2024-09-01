package com.example.css;
import com.intellij.icons.AllIcons;
import com.intellij.psi.css.impl.CssSelectorImpl;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;


final class CssModulesClassNameCompletionContributor extends CompletionContributor {

    CssModulesClassNameCompletionContributor(){
       var provider = new CompletionProvider<>() {
           @Override
           protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
               final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

               if(completionElement.getParent() instanceof JSLiteralExpression){
                  JSLiteralExpression literalExpression = (JSLiteralExpression) completionElement.getParent();
                  final PsiElement cssClassNamesImportOrRequire = CssModulesUtil.getCssClassNamesImportOrRequireDeclaration(literalExpression);
                  if(cssClassNamesImportOrRequire!=null) {
                      final StylesheetFile stylesheetFile = CssModulesUtil.resolveStyleSheetFile(cssClassNamesImportOrRequire);
                      if(stylesheetFile!=null) {
                          addCompletions(completionResultSet, stylesheetFile);
                      }
                  }
              }
           }

           private void addCompletions(
                   @NotNull CompletionResultSet resultSet,
                   @NotNull  StylesheetFile stylesheetFile
           ){
               String pre = null ; // &-app-selector;
               for(var cssClass : PsiTreeUtil.findChildrenOfAnyType(stylesheetFile,false,  CssSelectorImpl.class )) {
                   try{
                       String name = cssClass.getText();
                       String fileName = cssClass.getContainingFile().getName();
                       String curLine = String.valueOf(cssClass.getLineNumber());
                       if(name.startsWith("#") || name.startsWith(":global")) continue;
                       if(name.startsWith("&") && pre == null) continue;
                       name = name.replace("." , "");
                       if(name.startsWith("&") ) {
                           assert pre != null;
                           name = name.replace("&" , pre);
                       }
                       pre = name;
                       LookupElementBuilder element = LookupElementBuilder.create(name)
                               .withIcon(AllIcons.Xml.Css_class)
                               .withTypeText(cssClass.getPresentableText())
                                       .withTailText("(" + fileName + ":" + curLine + ")").withPsiElement(cssClass);
                       resultSet.addElement(element);
                   } catch (Exception e) {
                       throw new RuntimeException(e);
                   }
               }
           };
       };

       extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);
   }
}
