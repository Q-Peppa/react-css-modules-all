package com.example.document;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDocumentationProvider extends AbstractDocumentationProvider {

    public static void renderCssBlock(StringBuilder sb, CssDeclaration[] cssDeclarations) {
        for (CssDeclaration declaration : cssDeclarations) {
            if (declaration != null) {
                sb.append(DocumentationMarkup.PRE_ELEMENT.addText(StringUtil.trim(declaration.getText())));
            }
        }
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        StringBuilder content = new StringBuilder();
        content.append(DocumentationMarkup.DEFINITION_START);
        if (originalElement != null && originalElement.getParent() instanceof JSLiteralExpression js) {
            content.append(".").append(js.getStringValue());
            content.append(DocumentationMarkup.DEFINITION_END);
            content.append("<hr/>");
            content.append(DocumentationMarkup.SECTIONS_START);
            if (js.getReference() != null && js.getReference().resolve() instanceof CssSelector cssSelector) {
                CssRuleset ruleset = cssSelector.getRuleset();
                if (ruleset != null && ruleset.getBlock() != null) {
                    renderCssBlock(content, ruleset.getBlock().getDeclarations());
                }
            }
            content.append(DocumentationMarkup.SECTION_END);
        }
        return String.valueOf(content);
    }

    @Override
    public @Nullable @Nls String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

}