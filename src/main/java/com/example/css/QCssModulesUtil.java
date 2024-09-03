
package com.example.css;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Utility methods for navigating PSI trees with regards to CSS Modules.
 */
public class QCssModulesUtil {

    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final String SPACE = " ";
    /**
     * 需要连接父亲的名字
     */
    private static final String CONNECT_FLAG = "&";

    /**
     * @description 保存了 classname 和 CssSelector[] 的对应关系
     */
    public static final HashMap<String, CssSelector[]> psiElementRefHashMap = new HashMap<>();
    public static final HashSet<String> alreadyProcess = new HashSet<>();


    /**
     * PSI Pattern for matching string literals, e.g. the 'normal' in styles['normal']
     */
    public static final PsiElementPattern.Capture<JSLiteralExpression> STRING_PATTERN = PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
        @Override
        public boolean isAcceptable(Object element, @Nullable PsiElement context) {
            if (element instanceof JSLiteralExpression && context != null && context.getContainingFile() instanceof JSFile) {
                final ASTNode value = ((JSLiteralExpression) element).getNode().getFirstChildNode();
                return value != null && value.getElementType() == JSTokenTypes.STRING_LITERAL;
            }
            return false;
        }

        @Override
        public boolean isClassAcceptable(Class hintClass) {
            return JSLiteralExpression.class.isAssignableFrom(hintClass);
        }
    }));

    /**
     * Gets the CssClass PSI element whose name matches the specified cssClassName
     *
     * @param stylesheetFile the PSI style sheet file to visit
     * @param cssClass       the class to find, including the leading ".", e.g. ".my-class-name"
     * @return the matching class or <code>null</code> if no matches are found
     */
    public static CssSelector getCssClass(StylesheetFile stylesheetFile, String cssClass) {
        if (psiElementRefHashMap.containsKey(cssClass)) return psiElementRefHashMap.get(cssClass)[0];
        return PsiTreeUtil.getChildOfType(stylesheetFile, CssSelector.class);
    }


    /**
     * Gets the import/require declaration that a string literal belongs to, e.g 'normal' ->
     * 'const styles = require("./foo.css")' or 'import styles from "./foo.css"' based on <code>styles['normal']</code>
     *
     * @param classNameLiteral a string literal that is potentially a CSS class name
     * @return the JS variable that is a potential require of a style sheet file, or <code>null</code> if the PSI structure doesn't match
     */
    public static PsiElement getCssClassNamesImportOrRequireDeclaration(JSLiteralExpression classNameLiteral) {
        final JSIndexedPropertyAccessExpression expression = PsiTreeUtil.getParentOfType(classNameLiteral, JSIndexedPropertyAccessExpression.class);
        if (expression != null) {
            // string literal is part of "var['string literal']", e.g. "styles['normal']"
            if (expression.getQualifier() != null) {
                final PsiReference psiReference = expression.getQualifier().getReference();
                if (psiReference != null) {
                    final PsiElement varReference = psiReference.resolve();
                    if (varReference instanceof JSVariable) {
                        return varReference;
                    }
                    if (varReference instanceof ES6ImportedBinding) {
                        return varReference.getParent();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolves the style sheet PSI file that backs a require("./stylesheet.css").
     *
     * @param cssFileNameLiteralParent parent element to a file name string literal that points to a style sheet file
     * @return the matching style sheet PSI file, or <code>null</code> if the file can't be resolved
     */
    public static StylesheetFile resolveStyleSheetFile(PsiElement cssFileNameLiteralParent) {
        final Ref<StylesheetFile> stylesheetFileRef = new Ref<>();
        cssFileNameLiteralParent.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (stylesheetFileRef.get() != null) {
                    return;
                }
                if (element instanceof JSLiteralExpression) {
                    if (resolveStyleSheetFile(element, stylesheetFileRef)) return;
                }
                if (element instanceof ES6FromClause) {
                    if (resolveStyleSheetFile(element, stylesheetFileRef)) return;
                }
                super.visitElement(element);
            }
        });
        return stylesheetFileRef.get();
    }

    /**
     * Resolves a CssClass PSI element given a CSS filename and class name
     *
     * @param cssFileNameLiteralParent element which contains a require'd style sheet file
     * @param cssClass                 the CSS class to get including the "."
     * @param referencedStyleSheet     ref to set to the style sheet that any matching CSS class is declared in
     * @return the matching CSS class, or <code>null</code> in case the class is unknown
     */
    public static CssSelector getCssClass(PsiElement cssFileNameLiteralParent, String cssClass, Ref<StylesheetFile> referencedStyleSheet) {
        StylesheetFile stylesheetFile = resolveStyleSheetFile(cssFileNameLiteralParent);
        if (stylesheetFile != null) {
            referencedStyleSheet.set(stylesheetFile);
            return getCssClass(stylesheetFile, cssClass);
        } else {
            referencedStyleSheet.set(null);
            return null;
        }
    }

    /**
     * Gets the style sheet, if any, that the specified element resolves to
     *
     * @param element           element used to resolve
     * @param stylesheetFileRef the ref to set the resolved sheet on
     * @return true if the element resolves to a style sheet file, false otherwise
     */
    private static boolean resolveStyleSheetFile(PsiElement element, Ref<StylesheetFile> stylesheetFileRef) {
        for (PsiReference reference : element.getReferences()) {
            final PsiElement fileReference = reference.resolve();
            if (fileReference instanceof StylesheetFile) {
                stylesheetFileRef.set((StylesheetFile) fileReference);
                return true;
            }
        }
        return false;
    }

    private static void processMultiSelector(String className, HashSet<String> alreadyProcess, CssRuleset cssRuleset) {
        for (String s : className.split(className.contains(COMMA) ? COMMA : SPACE)) {
            alreadyProcess.add(StringUtil.trim(s));
            QCssModulesUtil.psiElementRefHashMap.put(s, cssRuleset.getSelectors());
        }
    }

    /**
     * 寻找当前CssRuleset最近,上一级的的CssRuleset
     * @param cssRuleset
     * @return CssRuleset
     */
    public static CssRuleset findPreFatherRuleSet(PsiElement cssRuleset) {
//         顶层， 没有父亲，返回自身
        if (cssRuleset.getParent() instanceof CssRulesetList) return (CssRuleset) cssRuleset;
        // 中间层， 可以找到父亲
        if (cssRuleset.getParent() instanceof CssRuleset) return (CssRuleset) cssRuleset.getParent();
        return findPreFatherRuleSet(cssRuleset.getParent());
//        return PsiTreeUtil.getParentOfType(cssRuleset, CssRuleset.class);
    }

    /**
     * 获得CssRuleset所有的类名
     * @example .lp , .demo{} => ["lp" , "demo"]
     * .lp{} =>["lp"]
     */
    public static LinkedList<String> getCssRuleSetAllNameWithDot(CssRuleset cssRuleset) {
        LinkedList<String> res = new LinkedList<>();
        if (cssRuleset == null) return res;
        for (CssSelector cssSelector : cssRuleset.getSelectors()) {
            res.add(cssSelector.getPresentableText());
        }
        return res;
    }

    public static String parseClass(CssRuleset cssClass) {
        String name = cssClass.getPresentableText();
        // 不处理任何其他情况， 仅仅处理 . 和 & 开头的选择器
        if (!name.startsWith(DOT) && !name.startsWith(CONNECT_FLAG)) return null;
        if (name.startsWith(":global")) return null;
        if (name.startsWith(DOT)) {
            CssRuleset father = QCssModulesUtil.findPreFatherRuleSet(cssClass);
            LinkedList<String> fatherName = QCssModulesUtil.getCssRuleSetAllNameWithDot(father);
            if (fatherName.size() == 1 && fatherName.getFirst().startsWith(":global")) return null;
            return name;
        }

        name = name.replaceFirst(CONNECT_FLAG, "");

        while (true) {
            CssRuleset father = QCssModulesUtil.findPreFatherRuleSet(cssClass);
            if (father instanceof CssStylesheet) break;
            LinkedList<String> fatherName = QCssModulesUtil.getCssRuleSetAllNameWithDot(father);

            final String cssSelectorName = fatherName.getFirst();
            if (cssSelectorName.startsWith(DOT)) {
                // ｜  .app{} ｜ .app .demo{} ｜  .app, .demo{} ｜  三种情况
                if (fatherName.size() == 1) {
                    if (!cssSelectorName.contains(SPACE))
                        // .app{}
                        return fatherName.getFirst() + name;
                    else {
                        // .app .xyz{}
                        String[] names = cssSelectorName.split(SPACE);
                        return names[names.length - 1] + name;
                    }
                }
                // .app , .xyz
                StringBuilder ans = new StringBuilder();
                for (String f : fatherName) {
                    ans.append(f).append(name).append(",");
                }
                return ans.substring(0, ans.length() - 1);
            } else {
                cssClass = father;
                name = fatherName.getLast() + name;
                name = name.replaceFirst(CONNECT_FLAG, "");
            }
        }

        return name;
    }

    /**
     * 代码预处理, 获取所有的类名, 以及类名对应的 PsiElement, 方便做代码跳转
     */
    public static void preLoad(@NotNull StylesheetFile stylesheetFile) {
        psiElementRefHashMap.clear();
        alreadyProcess.clear();
        for (CssRuleset cssClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssRuleset.class)) {
            try {
                if (isInTheGlobal(cssClass)) continue;
                String cssName = parseClass(cssClass);
                if (cssName == null) continue;
                if (cssName.contains(SPACE) || cssName.contains(COMMA)) {
                    processMultiSelector(cssName, alreadyProcess, cssClass);
                    continue;
                }
                alreadyProcess.add(StringUtil.trim(cssName));
                QCssModulesUtil.psiElementRefHashMap.put(cssName, cssClass.getSelectors());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * 判断某一个类是否为 :global 的子级, 无论深度
     * @param element
     * @return
     */
    public static boolean isInTheGlobal(PsiElement element){
        CssRuleset parent = (CssRuleset) PsiTreeUtil.findFirstParent(element, e ->
                e instanceof CssRuleset && ((CssRuleset) e).getPresentableText().contains(":global")
        );
        return parent != null;
    }
}
