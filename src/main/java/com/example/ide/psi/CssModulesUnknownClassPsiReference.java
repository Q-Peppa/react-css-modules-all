package com.example.ide.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.css.StylesheetFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CssModulesUnknownClassPsiReference extends PsiReferenceBase<PsiElement> {

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
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }

    public StylesheetFile getStylesheetFile() {
        return stylesheetFile;
    }
}
