# react-css-modules-all

## [Unreleased]

## [1.5.4] - 2025-12-10

- 升级版本号到 1.5.4 并准备发布
- improve CSS Modules class reference resolution and caching mechanism

## [1.5.3] - 2025-12-10

- 将插件名称从 "React Css Modules All" 更改为 "CSS Modules"

## [1.5.2] - 2025-12-10

- 将所有相关类从com.example.ide包迁移至com.peppa.css包
- 更新插件配置文件中的实现类路径
- 优化CssModulesClassAnnotator中的条件判断逻辑
- 改进SimpleCssSelectorFix中的光标定位和滚动逻辑
- 简化样式表文件解析方法并提取公共函数
- 移除冗余代码和不必要的导入语句
- 更新测试类的包声明以匹配新的结构

## [1.5.1] - 2025-12-09

- Extracted stylesheet parsing logic into a separate function `resolveStylesheetFromReference`
- Removed duplicate style file lookup code
- Optimized CSS selector caching mechanism
- Unified handling of JS reference expressions and literal expressions parsing
- Added null safety checks to avoid NullPointerException
- Cleaned up unused import statements

## [1.5.0] - 2025-12-08

### Bug Fixes

- fix: Quick fix for unknown CSS class now correctly refreshes annotations after adding new class
- fix: PSI reference caching issue - CSS class detection now dynamically resolves on each check
- fix: Completion priority setting was not taking effect (PrioritizedLookupElement return value was discarded)

### New Features

- feat: Support `styles.className` dot syntax for unknown class detection (previously only `styles['className']` was supported)
- feat: Quick fix now positions cursor at proper indentation inside the CSS block

### Code Quality

- refactor: Unified reference system with `CssModuleClassReference` for dynamic resolution
- refactor: Improved code style - use Kotlin primary constructor for `StylesInsertHandler`
- refactor: Renamed class `CssModulesIndexedStylesVarPsiReferenceContributorKt` to `CssModulesIndexedStylesVarPsiReferenceContributor`
- refactor: Made internal utilities private (`replaceLast`, `CLASS_NAME_FILTER`)
- chore: Updated JVM toolchain from 21 to 17 for broader compatibility

## [1.3.0] - 2025-05-08

- refactor: move css into completion

## [1.2.1] - 2025-04-11

- fix: fix some error completion not in JSIndexedPropertyAccessExpression

## [1.2.0] - 2025-04-11

- feat: now support completion with dot and auto wrap by single quote
- prettier: show quick doc prettier
- remove: remove unused import

## [1.1.0] - 2025-04-09

- break: improve platformVersion >= 2024.02
- break: improve jdk to 21 and kotlin to 2.1.20
- simplify: simplify build.config.kts
- performance: improve performance by find styleFile
- remove: remove unused import and code
- prettier: show quick doc prettier

## [1.0.3] - 2025-04-08

- fix: fix some error completion not in JSIndexedPropertyAccessExpression

## [1.0.2] - 2024-11-10

- update version number for jetbarins 2024.3

## [1.0.1] - 2024-10-10

- fix bug will case psi not work
- not call subtreeChanged, UI cost very expensive
- add getQuickNavigateInfo to polyfill mouse left info

## [1.0.0] - 2024-10-01

- fix bug not show completion when first active
- the plugin first stable version

## [0.0.12] - 2024-9-27

- use system function parse css
- re-write test file
- plugin.xml extensions simplify

## [0.0.11] - 2024-09-22

- temporary close SimpleDocumentationProvider function cause not test
- add code test for parse css/scss
- re-write plugin code use kotlin

## [0.0.10] - 2024-09-19

- fix fetal error : TypeScript type miss
- add plugin icon

## [0.0.9] - 2024-09-14

- fix bug with Compatibility Verification

## [0.0.8] - 2024-09-14

### New Feature

- support hover show documentation
- support & selector with tag

## [0.0.7] - 2024-09-10

### New Feature

- support selector with tag
- use intellij function deal with String and Container

## [0.0.6] - 2024-09-06

### New Feature

- remove all pseudo class / element

## [0.0.5] - 2024-09-05

### New Feature

- add some test case
- remove unused import
- support selector has SPACE

## [0.0.4] - 2024-09-04

### New Feature

- fix java compatibility problems

## [0.0.3] - 2024-09-04

### Changed zh_CN

- 支持了复杂一点的parents selector
- 支持了选择器含有伪类的情况

### Changed en_US

- support a little complex parents selector
- support css selector has pseudo

[Unreleased]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.5.4...HEAD
[1.5.4]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.5.3...v1.5.4
[1.5.3]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.5.2...v1.5.3
[1.5.2]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.5.1...v1.5.2
[1.5.1]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.3.0...v1.5.0
[1.4.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.2.1...v1.3.0
[1.2.1]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.0.3...v1.1.0
[1.0.3]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/Q-Peppa/react-css-modules-all/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.12...v1.0.0
[0.0.12]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.11...v0.0.12
[0.0.11]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.10...v0.0.11
[0.0.10]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.9...v0.0.10
[0.0.9]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/Q-Peppa/react-css-modules-all/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/Q-Peppa/react-css-modules-all/commits/v0.0.3
