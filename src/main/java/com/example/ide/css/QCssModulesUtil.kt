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
import com.intellij.psi.css.impl.CssSimpleSelectorImpl
import java.util.Objects

internal object QCssModulesUtil {


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

    private fun getCssClass(stylesheetFile:StylesheetFile, name:String): PsiElement?{
        var css:PsiElement? = null
        stylesheetFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (css != null) return;
                if (element is CssClass && element.text == name) {
                    css = element
                    return;
                }else if (element is CssSimpleSelectorImpl && element.text.startsWith("&")) {
                   // 可能需要查询的 name 是拼接来的
                    val  cssSimpleSelector = element as CssSimpleSelectorImpl
                    val path = mutableListOf<String>()
                    PsiTreeUtil.findFirstParent(cssSimpleSelector) {
                        if (it is CssRuleset) {
                            val cellName = it.presentableText.trim()
                            path.add(cellName)
                            cellName.startsWith(".") && !cellName.contains("&")
                        }
                        false
                    }
                    path[0] = cssSimpleSelector.presentableText.trim()
                    path.reverse()
                    val curConnectResult = QCssModuleParseUtil.getOriginCss(path)
                    if (curConnectResult.contains(".$name")) {
                        css = cssSimpleSelector
                        return
                    }

                }
                super.visitElement(element)
            }
        })
        return css
    }
    /**
     * 获取指定元素的 CSS 类选择器
     * @param element PsiElement 对象, 用于获取 StylesheetFile
     * @param cssClass CSS 类名 , 需要在 StylesheetFile 查询的类名 , 注意, cssClass不包含 . (点)
     * @param rs 用于存储解析后的 StylesheetFile 的引用
     * @return 对应的 CssSelector 对象，如果不存在则返回 null
     */
    fun getCssClass(element: PsiElement, cssClass: String, rs: Ref<StylesheetFile>): PsiElement? {
        val styleSheetFile = resolveStyleSheetFile(element)
        if (styleSheetFile == null) {
            rs.set(null)
            return null
        }
        // 如果不需要拼接, 那就直接可以查询到
        val child = findChild(cssClass, styleSheetFile)
        if (child != null) return PsiTreeUtil.getParentOfType(child, CssSelector::class.java)

        rs.set(styleSheetFile)
        // 理论来说, 进入这个 getCssClass , 那就是 selector 是 & 拼接形成的
        val selectors = getCssClass(styleSheetFile, cssClass)
        return selectors
    }

    private fun findChild(needQueryName: String, stylesheetFile: StylesheetFile): CssClass? {
        return PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass::class.java).find {
            it.name == needQueryName || it.nameIdentifier?.text == needQueryName
        }
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