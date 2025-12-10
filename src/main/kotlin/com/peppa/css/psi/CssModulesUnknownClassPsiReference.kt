package com.peppa.css.psi

import com.peppa.css.completion.restoreAllSelector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.css.StylesheetFile

/**
 * A dynamic PSI reference for CSS class names.
 */
class CssModuleClassReference @JvmOverloads constructor(
    element: PsiElement,
    val stylesheetFile: StylesheetFile,
    private val className: String,
    private val selectorProvider: (StylesheetFile) -> Map<String, SmartPsiElementPointer<PsiElement>> = { restoreAllSelector(it) },
    private val onResolve: (() -> Unit)? = null,
    private val enableCache: Boolean = true
) : PsiReferenceBase<PsiElement>(element) {

    // 基于 stylesheetFile.modificationStamp 的简单缓存，减少重复解析
    @Volatile
    private var cachedStamp: Long = -1L

    @Volatile
    private var cachedMap: Map<String, SmartPsiElementPointer<PsiElement>> = emptyMap()

    /**
     * Dynamically resolves the CSS class name. This is called each time the reference
     * needs to be resolved, ensuring fresh results after CSS file modifications.
     */
    override fun resolve(): PsiElement? {
        onResolve?.invoke()

        if (!enableCache) {
            // 每次都调用 provider；用于测试/比较场景
            return selectorProvider(stylesheetFile)[className]?.element
        }

        val currentStamp = stylesheetFile.modificationStamp
        if (currentStamp != cachedStamp) {
            synchronized(this) {
                val stampNow = stylesheetFile.modificationStamp
                if (stampNow != cachedStamp) {
                    cachedMap = selectorProvider(stylesheetFile)
                    cachedStamp = stampNow
                }
            }
        }
        return cachedMap[className]?.element
    }

    /**
     * Check if this reference points to an unknown (non-existent) CSS class.
     * Used by the annotator to show warnings.
     */
    fun isUnresolved(): Boolean = resolve() == null
}
