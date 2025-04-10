package com.example.ide.css

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

class StylesInsertHandler : InsertHandler<LookupElement> {
    private val needsBracketSyntax: Boolean;

    constructor(needsBracketSyntax: Boolean) {
        this.needsBracketSyntax = needsBracketSyntax

    }

    override fun handleInsert(
        context: InsertionContext,
        item: LookupElement
    ) {
        val editor = context.editor
        val document = editor.document
        val startOffset = context.startOffset
        val dotPosOffset = startOffset - 1
        val tailOffset = context.tailOffset
        if (needsBracketSyntax) {
            val lookupString = item.lookupString
            document.replaceString(dotPosOffset, tailOffset, "[$lookupString]")
            // move cursor to the end of the inserted text
            // 2 =  [ + ]
            editor.caretModel.moveToOffset(dotPosOffset + item.lookupString.length + 2)
        }
    }
}


