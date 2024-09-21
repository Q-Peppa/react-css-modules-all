package com.example.ide.css

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.util.text.Strings
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssRuleset
import com.intellij.psi.css.CssSelector
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.css.impl.CssSimpleSelectorImpl
import com.intellij.psi.util.PsiTreeUtil


class QCssModuleParseUtil {
    companion object {
        private val map: MutableMap<String, Array<CssSelector>> = mutableMapOf()
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
                val res: List<String> = QScssUtil.getOriginCss(reversed).filterNot {
                    it.contains("&") ||
                            it.isNullOrBlank() ||
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
         * stylesheetFile maybe is css , less , scss file
         */
        fun parseCssSelectorFormFile(stylesheetFile: StylesheetFile?): Array<String> {
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
    }

}