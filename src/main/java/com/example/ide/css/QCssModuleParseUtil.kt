package com.example.ide.css

import com.intellij.idea.LoggerFactory
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
import com.intellij.openapi.diagnostic.logger
import java.util.stream.Collectors

private val LOG = logger<QCssModuleParseUtil>()

internal object QCssModuleParseUtil {

    private const val COMMA: String = ","
    private const val EMPTY_STRING: String = ""
    private const val DOT: String = "."
    private const val SPACE: String = " "
    private const val CONNECT_FLAG: String = "&"
    private val contentMap = hashMapOf<String , MutableMap<String, Array<CssSelector>>>()


    private val realName: ArrayList<String> = arrayListOf<String>()
    /**
     * 解析scss,less 等高阶文件
     * @param stylesheetFile 样式表文件
     */
    private fun parseOtherFile(stylesheetFile: StylesheetFile, map: MutableMap<String, Array<CssSelector>>) {
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
    private fun parseCssFile(stylesheetFile: StylesheetFile,map: MutableMap<String, Array<CssSelector>>) {
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

    // @help wanted parseCssSelectorFormFile方法调用的次数太多了 , 计算 styles 文件的 hash 明显太耗时了 , 需要更好的办法
    // 目前缓存了文件的长度作为hash 的 key
    /**
     * 解析样式表文件, 此方法对外暴露, 外部调用的时候不需要关心是css还是scss还是less
     * @param stylesheetFile 样式表文件
     */
    @JvmStatic
    fun parseCssSelectorFormFile(stylesheetFile: StylesheetFile?): MutableMap<String, Array<CssSelector>> {
        if (stylesheetFile == null || PsiTreeUtil.hasErrorElements(stylesheetFile)) return mutableMapOf()
        if (contentMap.containsKey(stylesheetFile.text.length.toString())) return contentMap[stylesheetFile.text.length.toString()] ?: mutableMapOf()
        val map = mutableMapOf<String, Array<CssSelector>>()
        val fileName = stylesheetFile.name
        try {
            parseCssFile(stylesheetFile,map)
            if (fileName.matches(Regex(".*\\.css$", RegexOption.IGNORE_CASE))) {
                return map
            }
            parseOtherFile(stylesheetFile,map)
            return map
        }catch (e:Exception) {
            LOG.warn("code exception cause {} $e" ,e)
            return mutableMapOf()
        }finally {
            contentMap[stylesheetFile.text.length.toString()] = map;
        }
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

    fun getOriginCss(list: List<String>): ArrayList<String> {
        init();
        if (list.isEmpty()) return arrayListOf()
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