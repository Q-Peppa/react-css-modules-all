package com.peppa.css.completion

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.text.Strings
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.css.*
import com.intellij.psi.css.impl.CssEscapeUtil
import com.intellij.psi.css.impl.stubs.index.CssIndexUtil
import com.intellij.psi.css.util.CssCompletionUtil
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PathUtil

private fun recessivesClassInCssSelector(
    realSelector: CssSelector,
    cssSelector: CssSelector,
    map: MutableMap<String, SmartPsiElementPointer<PsiElement>>,
    pointerManager: SmartPointerManager
): Boolean {
    for (clazz in PsiTreeUtil.findChildrenOfType(cssSelector, CssClass::class.java)) {
        if (Strings.isEmpty(clazz.name)) continue
        realSelector.ruleset?.let { map[clazz.name!!] = pointerManager.createSmartPsiElementPointer(it) }
    }
    return true
}

/**
 * Return all available CSS class names in the stylesheet, mapping className → ruleset pointer.
 * Caches per file via CachedValuesManager.
 */
fun restoreAllSelector(stylesheetFile: StylesheetFile): Map<String, SmartPsiElementPointer<PsiElement>> {
    val pointerManager = SmartPointerManager.getInstance(stylesheetFile.project)

    return CachedValuesManager.getCachedValue(stylesheetFile) {
        val result = linkedMapOf<String, SmartPsiElementPointer<PsiElement>>()
        val scope = GlobalSearchScope.fileScope(stylesheetFile.project, stylesheetFile.virtualFile)

        // Resolve parent selector (&) for SCSS/LESS nesting
        CssIndexUtil.processAmpersandSelectors(stylesheetFile.project, scope) { selector ->
            selector.processAmpersandEvaluatedSelectors { evaluated ->
                recessivesClassInCssSelector(selector, evaluated, result, pointerManager)
            }
            true
        }

        // Resolve all class selectors
        CssIndexUtil.processAllSelectorSuffixes(
            CssSelectorSuffixType.CLASS,
            stylesheetFile.project,
            scope
        ) { name, css ->
            css.ruleset?.let { result[name] = pointerManager.createSmartPsiElementPointer(it) }
            true
        }

        CachedValueProvider.Result.create(result.toMap(), stylesheetFile)
    }
}


const val SpaceSize = 2
fun buildLookupElementHelper(
    name: String,
    css: PsiElement,
    location: String,
    isNeedWrapByChar: Boolean = false
): LookupElement {
    val lookupString = CssEscapeUtil.escapeSpecialCssChars(name)
    val lineNumber = (css as? CssRuleset)?.selectors?.firstOrNull()?.lineNumber ?: 0
    val lookup = if (isNeedWrapByChar) "'$lookupString'" else lookupString
    val ele = LookupElementBuilder.createWithSmartPointer(lookup, css)
        .bold()
        .withPsiElement(css)
        .withIcon(AllIcons.Xml.Css_class)
        .withPresentableText(lookup)
        .withCaseSensitivity(true)
        .withTailText(" ".repeat(SpaceSize) + "($location:$lineNumber)", true)

    return PrioritizedLookupElement.withPriority(ele, CssCompletionUtil.CSS_SELECTOR_SUFFIX_PRIORITY.toDouble())
}

private fun toGetStylesheetFile(ref: PsiReference?): StylesheetFile? {
    val resolved = ref?.resolve() ?: return null
    return when (resolved) {
        is StylesheetFile -> resolved
        is ES6ImportedBinding -> resolved.findReferencedElements().firstOrNull() as? StylesheetFile
        is PsiFile -> null
        else -> {
            (resolved as? JSReferenceExpression)?.reference?.resolve() as? StylesheetFile
        }
    }
}

fun resolveStylesheetFromReference(element: PsiElement?): StylesheetFile? = element?.let {
    when (it) {
        is JSLiteralExpression -> {
            val indexed = PsiTreeUtil.getParentOfType(it, JSIndexedPropertyAccessExpression::class.java)
            val candidateRef = (indexed?.firstChild as? JSReferenceExpression)
                ?: PsiTreeUtil.getParentOfType(it, JSReferenceExpression::class.java)
            toGetStylesheetFile(candidateRef?.reference)
        }
        is JSReferenceExpression -> toGetStylesheetFile(it.reference ?: it.firstChild?.reference)
        else -> null
    }
}

/**
 * @description: find style file by js variable
 *  foo["$1"] , $1 position is innerStringIndexPsiElement , the type should be JSLiteralExpression
 */
fun findReferenceStyleFile(innerStringIndexPsiElement: JSLiteralExpression?): StylesheetFile? =
    resolveStylesheetFromReference(innerStringIndexPsiElement)


fun generateLookupElementList(
    stylesheetFile: StylesheetFile,
    isDotCompletion: Boolean = false
): List<LookupElement> {
    val shortLocation =
        PathUtil.toSystemIndependentName(SymbolPresentationUtil.getFilePathPresentation(stylesheetFile))
    val allSelector = restoreAllSelector(stylesheetFile)
    val allLookupElement = allSelector.entries.mapNotNull {
        val psi = it.value.element ?: return@mapNotNull null
        buildLookupElementHelper(it.key, psi, shortLocation, isDotCompletion && it.key.contains("-"))
    }
    return allLookupElement
}
