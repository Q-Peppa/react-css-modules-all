package com.example.ide.annotator

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.css.StylesheetFile
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import org.jetbrains.annotations.NotNull

const val FAMILY_NAME = "Unknown class name"

class SimpleCssSelectorFix(private val key: String, private val stylesheetFile: StylesheetFile) :
    BaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun getText(): String = "$familyName .$key"

    override fun getFamilyName(): String = FAMILY_NAME

    override fun invoke(@NotNull project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val rulesetText = "\n.$key {\n    \n}"
        val ruleset = CssElementFactory.getInstance(project).createRuleset(
            rulesetText,
            stylesheetFile.language
        )
        val afterRuleSet = WriteCommandAction.runWriteCommandAction<PsiElement?>(project) {
            stylesheetFile.add(ruleset)
        } ?: return
        stylesheetFile.navigate(true)
        val offset = afterRuleSet.textOffset + rulesetText.indexOf("{") + 4
        FileEditorManager.getInstance(project).getEditors(stylesheetFile.virtualFile).forEach {
            if (it is TextEditor) {
                it.editor.caretModel.moveToOffset(offset)
            }
        }
        DaemonCodeAnalyzer.getInstance(project).restart(file)
    }
}