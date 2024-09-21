package com.example.ide.css

import com.intellij.lang.ecmascript6.psi.ES6FromClause
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssSelector
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssRuleset
import training.featuresSuggester.getParentOfType
import java.util.Objects

internal object QCssModulesUtil {


    /**
     * 获取指定 CSS 类的选择器
     * @param cssClass CSS 类名
     * @return 对应的 CssSelector 对象，如果不存在则返回 null
     */
    private fun getCssClass(cssClass: String, map: MutableMap<String, Array<CssSelector>>): CssSelector? {
        val selectors = map[cssClass]
        if (selectors?.isNotEmpty() == true) return selectors[0]
        return null;
    }

    /**
     * 获取import或require声明中的CSS类名
     * @param classNameLiteral CSS 类名的字面量表达式
     * @return 对应的 PsiElement 对象，如果不存在则返回 null
     */
    fun getCssClassNamesImportOrRequireDeclaration(classNameLiteral: JSLiteralExpression): PsiElement? {
        val expression = PsiTreeUtil.getParentOfType(classNameLiteral, JSIndexedPropertyAccessExpression::class.java)
        if (expression?.qualifier?.reference?.resolve() != null) {
            val varReference = expression.qualifier!!.reference!!.resolve()
            if (varReference is JSVariable) return varReference
            if (varReference is ES6ImportedBinding) return varReference.parent
        }
        return null;
    }

    /**
     * 解析样式表文件
     * @param cssFileNameLiteralParent CSS 文件名的字面量表达式的父元素
     * @return 对应的 StylesheetFile 对象，如果不存在则返回 null
     */
    fun resolveStyleSheetFile(cssFileNameLiteralParent: PsiElement): StylesheetFile? {
        val stylesheetFileRef = Ref<StylesheetFile>()
        cssFileNameLiteralParent.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (stylesheetFileRef.get() != null) return;
                if (element is JSLiteralExpression) {
                    if (resolveStyleSheetFile(element, stylesheetFileRef)) return;
                }
                if (element is ES6FromClause) {
                    if (resolveStyleSheetFile(element, stylesheetFileRef)) return;
                }
                super.visitElement(element)
            }
        })
        return stylesheetFileRef.get();
    }

    /**
     * 获取指定元素的 CSS 类选择器
     * @param element PsiElement 对象
     * @param cssClass CSS 类名
     * @param rs 用于存储解析后的 StylesheetFile 的引用
     * @return 对应的 CssSelector 对象，如果不存在则返回 null
     */
    fun getCssClass(element: PsiElement, cssClass: String, rs: Ref<StylesheetFile>): CssSelector? {
        val styleSheetFile = resolveStyleSheetFile(element)
        if (styleSheetFile == null) {
            rs.set(null)
            return null
        }
        val child = findChild(element, styleSheetFile)
        if (child != null) return PsiTreeUtil.getParentOfType(child, CssSelector::class.java)

        val mapCache = QCssModuleParseUtil.parseCssSelectorFormFile(styleSheetFile)


        rs.set(styleSheetFile)
        return getCssClass(cssClass, mapCache)
    }

    private fun findChild(element: PsiElement, stylesheetFile: StylesheetFile): CssClass? {
        for (clazz in PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass::class.java)) {
            if (clazz.text == element.text) return clazz
        }
        return null;
    }

    /**
     * 解析样式表文件
     * @param element PsiElement 对象
     * @param sr 用于存储解析后的 StylesheetFile 的引用
     * @return 如果解析成功则返回 true，否则返回 false
     */
    fun resolveStyleSheetFile(element: PsiElement, sr: Ref<StylesheetFile>): Boolean {
        for (reference in element.references) {
            val fileReference = reference.resolve()
            if (fileReference is StylesheetFile) {
                sr.set(fileReference)
                return true;
            }
        }
        return false
    }

    /**
     * 判断元素是否在:global{}范围内
     * @param element PsiElement 对象
     * @return 如果在:global{}范围内则返回 true，否则返回 false
     */
    fun isInTheGlobal(element: PsiElement): Boolean {
        val parent = PsiTreeUtil.findFirstParent(element) {
            it is CssRuleset && it.presentableText.contains(":global")
        }
        return Objects.nonNull(parent)
    }
}