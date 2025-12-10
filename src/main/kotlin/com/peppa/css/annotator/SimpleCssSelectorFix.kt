package com.peppa.css.annotator

import com.intellij.codeInsight.hints.declarative.impl.DeclarativeInlayHintsPassFactory
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.css.StylesheetFile
import org.jetbrains.annotations.NotNull

const val FAMILY_NAME = "Unknown class name"

class SimpleCssSelectorFix(private val key: String, private val stylesheetFile: StylesheetFile) :
    BaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun getText(): String = "$familyName .$key"

    override fun getFamilyName(): String = FAMILY_NAME

    override fun invoke(@NotNull project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return

        val rulesetText = "\n.$key {\n\t\t\n}"
        val ruleset = CssElementFactory.getInstance(project).createRuleset(
            rulesetText,
            stylesheetFile.language
        )

        stylesheetFile.navigate(true)
        stylesheetFile.add(ruleset)
        val newEditor = FileEditorManager.getInstance(project).selectedEditor ?: return;
        if (newEditor is TextEditor) {
            newEditor.editor.caretModel.moveToLogicalPosition(
                LogicalPosition(newEditor.editor.document.lineCount - 2, 0)
            )
            newEditor.editor.scrollingModel.scrollTo(
                newEditor.editor.caretModel.logicalPosition,
                ScrollType.MAKE_VISIBLE
            )
            DeclarativeInlayHintsPassFactory.scheduleRecompute(editor, project)
            DeclarativeInlayHintsPassFactory.scheduleRecompute(newEditor.editor, project)
        }
    }
}