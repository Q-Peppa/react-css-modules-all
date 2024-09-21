
package com.example.ide.css;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.util.Ref;
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

    /**
     * 保存了 classname 和 CssSelector[] 的对应关系
     * @example   .foo -> [CssSelector , CssSelector, ...]
     */
    public static final HashMap<String, CssSelector[]> psiElementRefHashMap = new HashMap<>();


    public static void initContainer() {
        psiElementRefHashMap.clear();
    }

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
    public static void refreshMap(StylesheetFile stylesheetFile){
        initContainer();
        CssModulesClassNameCompletionContributor.completionHelper(null , stylesheetFile);
    }

    /**
     * Gets the CssClass PSI element whose name matches the specified cssClassName
     *
     * @param cssClass  the class to find, including the leading ".", e.g. ".my-class-name"
     * @return the matching class or <code>null</code> if no matches are found
     */
    @Nullable
    public static CssSelector getCssClass( String cssClass) {
        if (psiElementRefHashMap.containsKey(cssClass)) return psiElementRefHashMap.get(cssClass)[0];
        return null;
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
            public void visitElement(@NotNull PsiElement element) {
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
    @Nullable
    public static CssSelector getCssClass(PsiElement cssFileNameLiteralParent, String cssClass, Ref<StylesheetFile> referencedStyleSheet) {
        StylesheetFile stylesheetFile = resolveStyleSheetFile(cssFileNameLiteralParent);
        if (stylesheetFile != null) {
            referencedStyleSheet.set(stylesheetFile);
            QCssModuleParseUtil.Companion.parseCssSelectorFormFile(stylesheetFile);
            return getCssClass(cssClass);
        } else {
            referencedStyleSheet.set(null);
            return null;
        }
    }

    /**
     * Gets the style sheet, if any, that the specified element resolves to
     *
     * @param element   element used to resolve
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

    /**
     * 判断某一个类是否为 :global 的子级, 无论深度
     * @return boolean
     */
    public static boolean isInTheGlobal(PsiElement element) {
        CssRuleset parent = (CssRuleset) PsiTreeUtil.findFirstParent(element, e ->
                e instanceof CssRuleset && ((CssRuleset) e).getPresentableText().contains(":global")
        );
        return parent != null;
    }

}
