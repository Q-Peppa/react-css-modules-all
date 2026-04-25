# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

```bash
# Build the plugin
./gradlew buildPlugin

# Run all tests
./gradlew check

# Run a single test class (use --tests with fully-qualified class name)
./gradlew test --tests "com.peppa.css.css.CssModulesClassNameCompletionContributorTest"

# Run plugin verification
./gradlew verifyPlugin

# Show plugin version
./gradlew properties --property version --quiet --console=plain
```

All commands use the Gradle wrapper (`./gradlew`). JVM 21 is required (set via `kotlin.jvmToolchain(21)`).

## Architecture

This is an IntelliJ Platform plugin that provides CSS Modules intelligence for JavaScript/TypeScript files. It adds four IDE capabilities, each implemented as a standard IntelliJ extension point registered in `src/main/resources/META-INF/plugin.xml`:

### Reference resolution (Ctrl+Click / Go to Definition)
**Entry:** `CssModulesIndexedStylesVarPsiReferenceContributor` — matches `JSLiteralExpression` inside `JSIndexedPropertyAccessExpression` (i.e., `styles['className']` bracket syntax). For each match it creates a `CssModuleClassReference`, which dynamically resolves the class name against the imported stylesheet via `restoreAllSelector()`.

**Key file:** `src/main/kotlin/com/peppa/css/psi/CssModulesUnknownClassPsiReference.kt` — `CssModuleClassReference` uses `modificationStamp`-based caching to avoid re-parsing the CSS on every resolve call. The caching can be toggled off via constructor parameter for testing.

### Code completion (Ctrl+Space)
**Entry:** `CssModulesClassNameCompletionContributor` — two providers:
- `IndexAccessCompletionProvider` for bracket syntax (`styles['...']`) — matches literal inside indexed access
- `DotAccessCompletionProvider` for dot syntax (`styles.className`) — matches reference expressions preceded by `.`; auto-converts hyphenated names to bracket syntax on insertion

### Annotation & quick fix (warnings on unknown classes)
**Entry:** `CssModulesClassAnnotator` — for both bracket and dot syntax, checks if the referenced class exists in the stylesheet. If not, shows a warning with `SimpleCssSelectorFix` which creates the missing CSS ruleset and navigates the editor to it.

### Hover documentation
**Entry:** `SimpleDocumentationProvider` — renders the CSS ruleset content when hovering over a class name in JS/TS.

### Core utility

`src/main/kotlin/com/peppa/css/completion/QCssModulesUtil.kt` is the central utility file:

- **`restoreAllSelector(stylesheetFile)`** — parses a stylesheet and returns a map of class name → pointer to CSS ruleset. Handles `&` parent selectors (SCSS/LESS) via `processAmpersandSelectors`, plain class selectors via `processAllSelectorSuffixes`, and recursively resolves `@import`/`@use` statements (with partial/underscore naming conventions and `~` node-style resolution).
- **`findReferenceStyleFile(element)`** — resolves a JS expression back to the `StylesheetFile` it imports (handles direct imports, default imports, re-exports).
- **`generateLookupElementList(stylesheetFile)`** — builds completion `LookupElement`s from the parsed selector map.

### Dependency chain for CSS class resolution

```
JS literal/reference → findReferenceStyleFile() → StylesheetFile
                                                  → restoreAllSelector() → Map<className, CssRuleset pointer>
                                                  → CssModuleClassReference.resolve()
```

## Plugin metadata

- Plugin ID: `react.css.module.all`
- Group: `com.peppa.css`
- Since-build: 242 (IntelliJ 2024.2+)
- Dependencies: `JavaScript` (bundled plugin), `com.intellij.css`
- Targets WebStorm 2024.2 for dependencies

## Tests

Tests extend `BasePlatformTestCase` and use fixture files under `src/test/resources/`. The pattern is:
1. Copy CSS files to project with `myFixture.copyFileToProject()`
2. Configure a JS file with `myFixture.configureByFile()`
3. Trigger IDE features (`myFixture.complete()`, etc.)
4. Assert on results

Test categories: completion (basic, media queries, mixins, tag selectors), reference caching/performance.
