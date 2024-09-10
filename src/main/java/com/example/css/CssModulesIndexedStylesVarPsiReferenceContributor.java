package com.example.css;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


/**
 * Adds a PSI references from an indexed string literal on a styles object to its corresponding class name.
 * For example, the 'normal' in styles['normal'] will point to the '.normal {}' CSS class in a requirement'd stylesheet.
 * If not Founded  , will point the First CssClassSelector in stylesheet
 */
public class CssModulesIndexedStylesVarPsiReferenceContributor extends PsiReferenceContributor {
    private static final String DOT = ".";
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(QCssModulesUtil.STRING_PATTERN, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                final PsiElement cssClassNamesImportOrRequire = QCssModulesUtil.getCssClassNamesImportOrRequireDeclaration((JSLiteralExpression) element);
                if (cssClassNamesImportOrRequire != null) {
                    final String literalClass = DOT + StringUtils.stripStart(StringUtils.stripEnd(element.getText(), "\"'"), "\"'");
                    final Ref<StylesheetFile> referencedStyleSheet = new Ref<>();
                    final PsiElement psiElement = QCssModulesUtil.getCssClass(cssClassNamesImportOrRequire, literalClass, referencedStyleSheet);
                    if (psiElement != null) {
                        return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {
                            @Override
                            public @NotNull PsiElement resolve() {
                                return psiElement;
                            }

                            @NotNull
                            @Override
                            public Object @NotNull [] getVariants() {
                                return new Object[0];
                            }
                        }};
                    }
                }
                return new PsiReference[0];
            }
        });
    }

}