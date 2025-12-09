package com.peppa.css.psi

import com.peppa.css.completion.restoreAllSelector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.css.StylesheetFile

/**
 * A dynamic PSI reference for CSS class names.
 * The resolve() method dynamically looks up the CSS selector each time it's called,
 * ensuring that changes to the CSS file are immediately reflected.
 */
class CssModuleClassReference(
    element: PsiElement,
    val stylesheetFile: StylesheetFile,
    private val className: String
) : PsiReferenceBase<PsiElement>(element) {

    /**
     * Dynamically resolves the CSS class name. This is called each time the reference
     * needs to be resolved, ensuring fresh results after CSS file modifications.
     */
    override fun resolve(): PsiElement? {
        val map = restoreAllSelector(stylesheetFile)
        return map[className]
    }

    /**
     * Check if this reference points to an unknown (non-existent) CSS class.
     * Used by the annotator to show warnings.
     */
    fun isUnresolved(): Boolean = resolve() == null
}

