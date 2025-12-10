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
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import java.io.File

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

private val IMPORT_STATEMENT_REGEX = Regex("@(?:import|use)[^;]*;")
private val QUOTED_PATH_REGEX = Regex("['\"]([^'\"]+)['\"]")

private fun resolveImportPaths(baseDir: File, projectBase: String?, importPath: String): List<File> {
    // Normalize path segments
    val cleanPath = importPath.replace("\\", "/")
    val lastSlash = cleanPath.lastIndexOf('/')
    val dirPart = if (lastSlash >= 0) cleanPath.take(lastSlash) else ""
    val namePart = if (lastSlash >= 0) cleanPath.substring(lastSlash + 1) else cleanPath

    val candidates = mutableListOf<String>()
    val exts = listOf(".scss", ".sass", ".css")

    // If importPath already has an extension, prefer that exact file and its partial
    val hasExt = exts.any { namePart.endsWith(it) }
    if (hasExt) {
        candidates += cleanPath
        // add partial with underscore
        val idx = namePart.lastIndexOf('.')
        val bare = if (idx >= 0) namePart.take(idx) else namePart
        val ext = if (idx >= 0) namePart.substring(idx) else ""
        val partial = if (dirPart.isEmpty()) "_${bare}${ext}" else "$dirPart/_${bare}${ext}"
        candidates += partial
    } else {
        for (ext in exts) {
            val p1 = if (dirPart.isEmpty()) "$namePart$ext" else "$dirPart/$namePart$ext"
            val p2 = if (dirPart.isEmpty()) "_${namePart}$ext" else "$dirPart/_${namePart}$ext"
            candidates += listOf(p1, p2)
        }
    }

    // Also consider node-style ~ resolution relative to project base
    val files = mutableListOf<File>()
    for (c in candidates) {
        // relative to baseDir
        files += File(baseDir, c)
        // if project base provided and import starts with ~ or not found, try project base
        if (projectBase != null) {
            files += File(projectBase, c)
            if (c.startsWith("~")) files += File(projectBase, c.removePrefix("~"))
        }
    }
    return files
}

/**
 * return all available css className in file map , foo-> some ruleset pointer
 * 支持递归解析 scss 的 @import 与 @use（有限的相对路径、partial 和扩展名尝试）
 */
fun restoreAllSelector(stylesheetFile: StylesheetFile): Map<String, SmartPsiElementPointer<PsiElement>> {
    val pointerManager = SmartPointerManager.getInstance(stylesheetFile.project)
    val psiManager = PsiManager.getInstance(stylesheetFile.project)
    val projectBase = stylesheetFile.project.basePath

    return CachedValuesManager.getCachedValue(stylesheetFile) {
        val of = mutableMapOf<String, SmartPsiElementPointer<PsiElement>>()
        val scope = GlobalSearchScope.fileScope(stylesheetFile.project, stylesheetFile.virtualFile)
        val dependencyFiles = mutableListOf<Any>(stylesheetFile)

        // 处理当前文件的选择器
        CssIndexUtil.processAmpersandSelectors(stylesheetFile.project, scope) { selector ->
            selector.processAmpersandEvaluatedSelectors { evaluated ->
                recessivesClassInCssSelector(selector, evaluated, of, pointerManager)
            }
            true
        }
        CssIndexUtil.processAllSelectorSuffixes(
            CssSelectorSuffixType.CLASS,
            stylesheetFile.project,
            scope
        ) { name, css ->
            css.ruleset?.let { of[name] = pointerManager.createSmartPsiElementPointer(it) }
            true
        }

        // 解析 @import / @use，递归合并
        try {
            val virtualFile = stylesheetFile.virtualFile
            val baseDir = virtualFile?.let { File(it.path).parentFile } ?: File(stylesheetFile.containingDirectory?.virtualFile?.path ?: projectBase ?: "")
            val text = stylesheetFile.text

            // 找到所有 @import/@use 语句，然后提取每个语句中的所有引号路径
            val imports = IMPORT_STATEMENT_REGEX.findAll(text).flatMap { stmt ->
                QUOTED_PATH_REGEX.findAll(stmt.value).mapNotNull { it.groups[1]?.value }
            }.toList()

            val visited = mutableSetOf<String>()

            fun collectFromImported(path: String) {
                val candidates = resolveImportPaths(baseDir, projectBase, path)
                for (f in candidates) {
                    val abs = try { f.canonicalPath } catch (_: Exception) { f.absolutePath }
                    if (abs in visited) continue
                    if (!f.exists()) continue
                    val vf = LocalFileSystem.getInstance().findFileByIoFile(f) ?: continue
                    val psi = psiManager.findFile(vf) as? StylesheetFile ?: continue
                    visited += abs
                    dependencyFiles += psi
                    // 合并被导入文件的选择器（利用被导入文件自身的缓存）
                    val imported = restoreAllSelector(psi)
                    for ((k, ptr) in imported) of.putIfAbsent(k, ptr)
                }
            }

            for (imp in imports) collectFromImported(imp)
        } catch (_: Exception) {
            // 保守处理：若解析导入失败，不阻塞主流程
        }

        CachedValueProvider.Result.create(of.toMap(), *dependencyFiles.toTypedArray())
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
    val lineNumber = (css as CssRuleset).selectors.first().lineNumber
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
    // 增强解析逻辑：支持直接 resolve 到 StylesheetFile、PsiFile，或 ES6ImportedBinding，
    // 并尝试沿引用链继续解析，提升命中率和鲁棒性。
    val resolved = ref?.resolve() ?: return null
    return when (resolved) {
        is StylesheetFile -> resolved
        is ES6ImportedBinding -> resolved.findReferencedElements().firstOrNull() as? StylesheetFile
        is PsiFile -> null
        else -> {
            // 作为兜底：如果 resolve 到了一个引用表达式（alias 等），尝试继续解析一次
            (resolved as? JSReferenceExpression)?.reference?.resolve() as? StylesheetFile
        }
    }
}

fun resolveStylesheetFromReference(element: PsiElement?): StylesheetFile? {
    if (element == null) return null

    // 字符串字面量的情况：查找最近的引用表达式（支持多种父级结构）
    if (element is JSLiteralExpression) {
        // 优先查找索引访问 foo["bar"] 的首子表达式
        val indexed = PsiTreeUtil.getParentOfType(element, JSIndexedPropertyAccessExpression::class.java)
        val candidateRef = (indexed?.firstChild as? JSReferenceExpression)
            // 否则向上查找通用的 JSReferenceExpression（例如 foo.bar 或 更复杂结构）
            ?: PsiTreeUtil.getParentOfType(element, JSReferenceExpression::class.java)

        return toGetStylesheetFile(candidateRef?.reference)
    }

    // 直接是引用表达式：优先用自身的 reference，再退回到 firstChild 的 reference（兼容旧逻辑）
    if (element is JSReferenceExpression) {
        return toGetStylesheetFile(element.reference ?: element.firstChild?.reference)
    }

    return null
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