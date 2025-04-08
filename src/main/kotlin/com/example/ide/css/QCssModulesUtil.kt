package com.example.ide.css

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.text.Strings
import com.intellij.psi.PsiElement
import com.intellij.psi.css.*
import com.intellij.psi.css.impl.CssEscapeUtil
import com.intellij.psi.css.impl.stubs.index.CssIndexUtil
import com.intellij.psi.css.util.CssCompletionUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

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
fun restoreAllSelector(stylesheetFile: StylesheetFile): MutableMap<String, PsiElement> {
    val of = mutableMapOf<String, PsiElement>()
    val scope = GlobalSearchScope.fileScope(stylesheetFile.project, stylesheetFile.virtualFile)
    CssIndexUtil.processAmpersandSelectors(stylesheetFile.project, scope) {
        // realSelector in file , afterResolve is dummy file, can't find resolve and lineNumber
        val realSelector = it
        it.processAmpersandEvaluatedSelectors { afterResolve ->
            recessivesClassInCssSelector(realSelector, afterResolve, of)
        }
        true
    }
    CssIndexUtil.processAllSelectorSuffixes(CssSelectorSuffixType.CLASS, stylesheetFile.project, scope) { name, css ->
        of[name] = css.ruleset!!
        true
    }
    return of
}

/**
 * the number more big , the completion lookup more up
 */
const val SpaceSize = 2
fun buildLookupElementHelper(name: String, css: PsiElement, location: String): LookupElementBuilder {
    val lookupString = CssEscapeUtil.escapeSpecialCssChars(name)
    val lineNumber = (css as CssRuleset).selectors.first().lineNumber
    val ele = LookupElementBuilder.createWithSmartPointer(lookupString, css)
        .bold()
        .withPsiElement(css)
        .withIcon(AllIcons.Xml.Css_class)
        .withPresentableText(name)
        .withCaseSensitivity(true)
        .withTailText(" ".repeat(SpaceSize) + "($location:$lineNumber)", true)
    PrioritizedLookupElement.withPriority(ele, CssCompletionUtil.CSS_SELECTOR_SUFFIX_PRIORITY.toDouble())
    return ele
}

/**
 * @description: find style file by js variable
 *  foo["$1"] , $1 position is innerStringIndexPsiElement , the type should be JSLiteralExpression
 */
fun findReferenceStyleFile(innerStringIndexPsiElement: JSLiteralExpression?): StylesheetFile? {
    if (innerStringIndexPsiElement == null) return null
    val callKey = innerStringIndexPsiElement.parent?.firstChild // by style["$1"] get styles
    if (callKey !is JSReferenceExpression) return null
    val element = callKey.reference?.resolve() // will return import foo from  "bar"
    if (element !is ES6ImportedBinding || element.findReferencedElements().isEmpty()) return null
    val first = element.findReferencedElements().first()
    return if (first is StylesheetFile) first else null
}