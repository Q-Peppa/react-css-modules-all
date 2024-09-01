package com.example.css;


import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

 class CssModulesUnknownClassPsiReference extends PsiReferenceBase<PsiElement> {

    private final StylesheetFile stylesheetFile;

    public CssModulesUnknownClassPsiReference(@NotNull PsiElement element, TextRange rangeInElement, StylesheetFile stylesheetFile) {
        super(element, rangeInElement);
        this.stylesheetFile = stylesheetFile;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // self reference to prevent JS tooling from reporting unresolved symbol
        return this.getElement();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    public StylesheetFile getStylesheetFile() {
        return stylesheetFile;
    }
}

/**
 * Adds a PSI references from an indexed string literal on a styles object to its corresponding class name.
 * For example, the 'normal' in styles['normal'] will point to the '.normal {}' CSS class in a requirement'd stylesheet.
 */
public class CssModulesIndexedStylesVarPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(CssModulesUtil.STRING_PATTERN, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                final PsiElement cssClassNamesImportOrRequire = CssModulesUtil.getCssClassNamesImportOrRequireDeclaration((JSLiteralExpression) element);
                if (cssClassNamesImportOrRequire != null) {
                    final String literalClass = "." + StringUtils.stripStart(StringUtils.stripEnd(element.getText(), "\"'"), "\"'");
                    final Ref<StylesheetFile> referencedStyleSheet = new Ref<>();
                    final CssSimpleSelector cssClass = CssModulesUtil.getCssClass(cssClassNamesImportOrRequire, literalClass, referencedStyleSheet);
                    if (cssClass != null) {
                        return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {
                            @Override
                            public @NotNull PsiElement resolve() {
                                return cssClass;
                            }

                            @NotNull
                            @Override
                            public Object @NotNull [] getVariants() {
                                return new Object[0];
                            }
                        }};
                    } else {
                        if (referencedStyleSheet.get() != null) {
                            final TextRange rangeInElement = TextRange.from(1, element.getTextLength() - 2); // minus string quotes
                            return new PsiReference[]{new CssModulesUnknownClassPsiReference(element, rangeInElement, referencedStyleSheet.get())};
                        }
                    }

                }
                return new PsiReference[0];
            }
        });
    }

}