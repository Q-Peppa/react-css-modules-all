package com.example.ide.css

import com.intellij.openapi.util.text.Strings
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssRuleset
import com.intellij.psi.css.CssSelector
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.css.impl.CssSimpleSelectorImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


internal object QCssModuleParseUtil {
    private const val COMMA: String = ","
    private const val EMPTY_STRING: String = ""
    private const val DOT: String = "."
    private const val SPACE: String = " "
    private const val CONNECT_FLAG: String = "&"

    @JvmStatic
    val map: MutableMap<String, Array<CssSelector>> = mutableMapOf()
    private val realName: ArrayList<String> = arrayListOf<String>()
    /**
     * 解析scss,less 等高阶文件
     * @param stylesheetFile 样式表文件
     */
    private fun parseOtherFile(stylesheetFile: StylesheetFile) {
        for (simpleSelector in PsiTreeUtil.findChildrenOfType(
            stylesheetFile,
            CssSimpleSelectorImpl::class.java
        )) {

            val text = Strings.trim(simpleSelector.presentableText) ?: ""
            if (text.isBlank() || text.length <= 1 || !text.startsWith("&")) continue
            if (text.startsWith("&:")) continue;
            val path = mutableListOf<String>()
            PsiTreeUtil.findFirstParent(simpleSelector) {
                if (it is CssRuleset) {
                    val cellName = it.presentableText.trim()
                    path.add(cellName)
                    cellName.startsWith(".") && !cellName.contains("&")
                }
                false
            }
            val psiElement = simpleSelector.parent as CssSelector
            if (path.isEmpty() || path.size <= 1) continue
            path[0] = text
            val reversed = path.reversed()
            val res: List<String> = getOriginCss(reversed).filterNot {
                it.contains("&") ||
                        it.isBlank() ||
                        map.contains(it.trim()) || !it.startsWith(".")

            }.map {
                if (it.contains(":")) it.split(":").last() else it
            }

            res.forEach {
                map[it.replaceFirst(".", "")] = arrayOf(psiElement)
            }

        }
    }

    /**
     * 解析css文件
     * @param stylesheetFile 样式表文件
     */
    private fun parseCssFile(stylesheetFile: StylesheetFile) {
        for (cssClass in PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass::class.java)) {
            if (QCssModulesUtil.isInTheGlobal(cssClass)) continue;
            val name = Strings.trim(cssClass.name) ?: ""
            if (map.contains(name)) continue
            val ruleset = cssClass.ruleset
            ruleset?.let {
                val selectors = it.selectors
                map.put(
                    key = name.replaceFirst(".", ""),
                    value = selectors
                )
            }
        }
    }
    /**
     * 解析样式表文件, 此方法对外暴露, 外部调用的时候不需要关心是css还是scss还是less
     * @param stylesheetFile 样式表文件
     */
    fun parseCssSelectorFormFile(stylesheetFile: StylesheetFile?): Array<String> {
        println("parseCssSelectorFormFile will be  invoke name is ${stylesheetFile?.name}")
        if (stylesheetFile == null || PsiTreeUtil.hasErrorElements(stylesheetFile)) return arrayOf()
        map.clear()
        val fileName = stylesheetFile.name
        parseCssFile(stylesheetFile)
        if (fileName.matches(Regex(".*\\.css$", RegexOption.IGNORE_CASE))) {
            println("parseCssFile end , ans = ${map.keys.toList()}")
            return map.keys.toTypedArray()
        }
        parseOtherFile(stylesheetFile)
        println("parseOtherFile end , ans = ${map.keys.toList()}")
        return map.keys.toTypedArray()
    }



    private fun traverse(headClass: String, list: List<String>, pos: Int): String {
        var head = headClass
        head = StringUtils.trim(head)
        if (head.contains(SPACE)) {
            head = head.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[head.split(
                SPACE.toRegex()
            ).dropLastWhile { it.isEmpty() }.toTypedArray().size - 1]
        }
        if (head.contains(DOT) && head.indexOf(DOT) != 0) {
            head = DOT + StringUtils.substringAfterLast(head, DOT)
        }
        if (pos >= list.size) {
            realName.add(head)
            return EMPTY_STRING.toString()
        }
        val now = list[pos]
        val newHead = StringBuilder(now)
        if (!now.contains(COMMA)) {
            var sb = ArrayUtil.getLastElement(now.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            sb = sb.replaceFirst(CONNECT_FLAG.toRegex(), head)
            sb += (traverse(sb, list, pos + 1))
            return sb
        }

        for (stringVal in newHead.toString().split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()) {
            var string = stringVal
            string = StringUtils.trim(string)
            traverse(string.replace(CONNECT_FLAG.toRegex(), head), list, pos + 1)
        }
        return newHead.toString()
    }

    private fun init() {
        realName.clear()
    }

    private fun getOriginCss(list: List<String>): ArrayList<String> {
        init();
        val head = ContainerUtil.getFirstItem(list)
        val strings = head.split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (head.contains(SPACE) && !head.contains(COMMA)) {
            val sb = ArrayUtil.getLastElement(head.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            traverse(sb, list, 1)
        } else {
            for (string in strings) {
                traverse(string, list, 1)
            }
        }
        return realName
    }

    /**
     * if getOriginCss not work , try this;
     * @param selectors 一些列的选择器
     * @return 最终解析的结果
     */
    private  fun getOriginCss2(selectors: List<String>?): List<String> {
        if (selectors == null || selectors.isEmpty()) {
            return listOf()
        }

        val result = selectors.stream().reduce { acc: String, curr: String ->
            if (acc.isEmpty() || !curr.contains(CONNECT_FLAG)) {
                return@reduce curr
            }
            val parents =
                Arrays.stream(acc.split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    .map { obj: String -> obj.trim { it <= ' ' } }
                    .map { s: String ->
                        s.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    }
                    .map { arr: Array<String> -> arr[arr.size - 1] }
                    .toList()
            Arrays.stream(curr.split(COMMA.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .flatMap { part: String ->
                    parents.stream()
                        .map { parent: String? -> (part.trim { it <= ' ' }.replace(CONNECT_FLAG, parent!!)) }
                }
                .collect(Collectors.joining(COMMA + SPACE))
        }.orElse(EMPTY_STRING)

        return Arrays.stream(result.split((COMMA + SPACE).toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray())
            .filter { s: String -> !s.isEmpty() }
            .toList()
    }

}