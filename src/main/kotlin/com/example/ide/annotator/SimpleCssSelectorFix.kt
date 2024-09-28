package com.example.ide.annotator

import com.example.ide.message.QCssMessageBundle
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.css.StylesheetFile
import org.jetbrains.annotations.NotNull

class SimpleCssSelectorFix(private val key: String, private val stylesheetFile: StylesheetFile) :
    BaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun getText(): String = "$familyName .$key"

    override fun getFamilyName(): String = QCssMessageBundle.message("familyName")

    override fun invoke(@NotNull project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val ruleset = CssElementFactory.getInstance(project).createRuleset(
            "\n.$key {\n\n}",
            stylesheetFile.language
        )
        val afterRuleSet = stylesheetFile.add(ruleset)!!
        stylesheetFile.navigate(true)
        val offset = afterRuleSet.textOffset + ruleset.text.indexOf("{") + 2
        FileEditorManager.getInstance(project).getEditors(stylesheetFile.virtualFile).forEach {
            if (it is TextEditor) {
                it.editor.caretModel.moveToOffset(offset)
            }
        }
    }
}