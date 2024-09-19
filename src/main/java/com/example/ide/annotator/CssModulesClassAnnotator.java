package com.example.ide.annotator;


import com.example.ide.css.*;
import com.example.ide.psi.CssModulesUnknownClassPsiReference;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssElementFactory;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;


class SimpleCssFix extends BaseIntentionAction {
    private final String key;
    private final StylesheetFile stylesheetFile;

    SimpleCssFix(String key , StylesheetFile stylesheetFile) {
        this.stylesheetFile = stylesheetFile;
        this.key = key;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return getFamilyName() + " ." + this.key;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        PsiElement ruleset = CssElementFactory.getInstance(project).createRuleset("\n."+this.key+"{}", this.stylesheetFile.getLanguage());
        this.stylesheetFile.add(ruleset);
        this.stylesheetFile.navigate(true);
        psiFile.clearCaches();
        // TODO: move Input Start inside ruleset
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return QCssMessage.message("FamilyName");
    }
}

public class CssModulesClassAnnotator implements Annotator {


    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        PsiElement elementToAnnotate = null;
        if (psiElement instanceof JSLiteralExpression) {
            elementToAnnotate = psiElement;
        }
        if (elementToAnnotate != null) {
            for (PsiReference psiReference : psiElement.getReferences()) {
                if (psiReference instanceof CssModulesUnknownClassPsiReference un) {
                    final TextRange rangeInElement = psiReference.getRangeInElement();
                    if (rangeInElement.isEmpty()) {
                        continue;
                    }
                    int start = psiElement.getTextRange().getStartOffset() + rangeInElement.getStartOffset();
                    int length = rangeInElement.getLength();
                    final TextRange textRange = TextRange.from(start, length);
                    if (!textRange.isEmpty()) {
                        final String message = "Unknown class name \"" + rangeInElement.substring(psiElement.getText()) + "\"";
                        annotationHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, message).range(textRange).withFix(new SimpleCssFix(
                                rangeInElement.substring(psiElement.getText()),
                                un.getStylesheetFile()
                        )).create();
                    }
                }
            }
        }
    }
}