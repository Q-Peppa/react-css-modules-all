package com.example.ide.document;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDocumentationProvider extends AbstractDocumentationProvider {

    private @Nullable String renderDoc(@NotNull CssSelector cssSelector, @Nullable String selectorName) {

        if (StringUtil.isEmptyOrSpaces(selectorName)) {
            selectorName = cssSelector.getText();
        }
        StringBuilder content = new StringBuilder();
        content.append(DocumentationMarkup.DEFINITION_START);
        content.append(".").append(selectorName);
        content.append(DocumentationMarkup.DEFINITION_END);
        content.append("<hr/>");
        content.append(DocumentationMarkup.SECTIONS_START);
        CssRuleset cssRuleset = cssSelector.getRuleset();
        if (cssRuleset != null && cssRuleset.getBlock() != null) {
            for (CssDeclaration declaration : cssRuleset.getBlock().getDeclarations()) {
                if (declaration != null) {
                    content.append(HtmlChunk.tag("pre").addText(StringUtil.trim(declaration.getText())));
                }
            }
        }
        content.append(DocumentationMarkup.SECTION_END);
        content.append("<hr/>");
        return String.valueOf(content);

    }

    /**
     * @example const wrapper = styles['app-foo']  <br/>
     * hover wrapper and 'app-foo' will show style content;
     */
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {

        JSLiteralExpression expression = PsiTreeUtil.findChildOfType(element, JSLiteralExpression.class);
        // deal with parameter
        if (expression != null && expression.getReference() != null && expression.getReference().resolve() instanceof CssSelector cssSelector) {
            return renderDoc(cssSelector, expression.getStringValue());
        }

        /**
         * @issue fix https://github.com/Q-Peppa/react-css-modules-all/issues/1
         */
        // deal with key
        if (element instanceof CssSelector && originalElement != null && originalElement.getParent() instanceof JSLiteralExpression js) {
            String doc = "";
            if (js.getReference() != null && js.getReference().resolve() instanceof CssSelector cssSelector) {
                doc = renderDoc(cssSelector, js.getStringValue());
            }
            return StringUtil.isEmpty(doc) ? super.generateDoc(element, originalElement) : doc;
        }
        return super.generateDoc(element, originalElement);
    }

    @Override
    public @Nullable @Nls String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

}