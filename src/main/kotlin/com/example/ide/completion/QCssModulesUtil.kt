package com.example.ide.completion

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
import com.intellij.psi.css.*
import com.intellij.psi.css.impl.CssEscapeUtil
import com.intellij.psi.css.impl.stubs.index.CssIndexUtil
import com.intellij.psi.css.util.CssCompletionUtil
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PathUtil

private fun recessivesClassInCssSelector(
    realSelector: CssSelector,
    cssSelector: CssSelector,
    map: MutableMap<String, PsiElement>
): Boolean {
    for (clazz in PsiTreeUtil.findChildrenOfType(cssSelector, CssClass::class.java)) {
        if (Strings.isEmpty(clazz.name)) continue
        map[clazz.name!!] = realSelector.ruleset!!
    }
    return true
}

/**
 * return all available css className in file map , foo-> some ruleset
 */
fun restoreAllSelector(stylesheetFile: StylesheetFile): MutableMap<String, PsiElement> =
    CachedValuesManager.getCachedValue(stylesheetFile) {
        val of = mutableMapOf<String, PsiElement>()
        val scope = GlobalSearchScope.fileScope(stylesheetFile.project, stylesheetFile.virtualFile)

        CssIndexUtil.processAmpersandSelectors(stylesheetFile.project, scope) { selector ->
            selector.processAmpersandEvaluatedSelectors { evaluated ->
                recessivesClassInCssSelector(selector, evaluated, of)
            }
            true
        }
        CssIndexUtil.processAllSelectorSuffixes(CssSelectorSuffixType.CLASS, stylesheetFile.project, scope) { name, css ->
            of[name] = css.ruleset!!
            true
        }

        CachedValueProvider.Result.create(of, stylesheetFile, PsiModificationTracker.MODIFICATION_COUNT)
    }


const val SpaceSize = 2
fun buildLookupElementHelper(
    name: String,
    css: PsiElement,
    location: String,
    isNeedWrapByChar: Boolean = false
): LookupElement {
    val lookupString = CssEscapeUtil.escapeSpecialCssChars(name)
    val lineNumber = (css as CssRuleset).selectors.first().lineNumber
    val lookup = if (isNeedWrapByChar) "'$lookupString'" else lookupString
    val ele = LookupElementBuilder.createWithSmartPointer(lookup, css)
        .bold()
        .withPsiElement(css)
        .withIcon(AllIcons.Xml.Css_class)
        //  the PresentableText will be wrap by ' ' if name has - ;
        .withPresentableText(lookup)
        .withCaseSensitivity(true)
        .withTailText(" ".repeat(SpaceSize) + "($location:$lineNumber)", true)

    return PrioritizedLookupElement.withPriority(ele, CssCompletionUtil.CSS_SELECTOR_SUFFIX_PRIORITY.toDouble())
}

/**
 * 解析引用处的样式文件。支持 styles["xxx"] 与 styles.xxx 两种用法，集中解析以避免重复逻辑。
 */
fun resolveStylesheetFromReference(element: PsiElement?): StylesheetFile? = when (element) {
    is JSLiteralExpression -> {
        // styles["xxx"]
        val callKey = (element.parent as? JSIndexedPropertyAccessExpression)?.firstChild as? JSReferenceExpression
            ?: return null
        val binding = callKey.reference?.resolve() as? ES6ImportedBinding ?: return null
        binding.findReferencedElements().firstOrNull() as? StylesheetFile
    }
    is JSReferenceExpression -> {
        // styles.xxx
        val binding = element.firstChild?.reference?.resolve() as? ES6ImportedBinding ?: return null
        binding.findReferencedElements().firstOrNull() as? StylesheetFile
    }
    else -> null
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
    val allLookupElement = allSelector.entries.map {
        buildLookupElementHelper(it.key, it.value, shortLocation, isDotCompletion && it.key.contains("-"))
    }
    return allLookupElement
}