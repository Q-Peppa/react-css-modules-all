# CSS Modules

![Downloads](https://img.shields.io/jetbrains/plugin/d/react.css.module.all)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/react.css.module.all)
![Version](https://img.shields.io/jetbrains/plugin/v/react.css.module.all)

IntelliJ/WebStorm plugin for CSS Modules in JavaScript and TypeScript. Provides code completion, go-to-definition navigation, quick documentation, and missing-class quick fixes for `import styles from './file.css'`.

## Features

- **Completion** — class name completion for bracket syntax (`styles['...']`) and dot syntax (`styles....`); hyphenated names auto-convert to bracket syntax on insertion
- **Navigation** — Ctrl+Click / Go to Definition on a class name jumps to the CSS ruleset
- **Quick documentation** — hover over a class name to see the corresponding CSS block
- **Annotations** — unknown class names are highlighted with a quick fix to create the missing ruleset
- **Sass/SCSS/LESS** — resolves parent selectors (`&`), `@import`, `@use`, and `@forward`
- **Circular import safety** — gracefully short-circuits circular `@import`/`@use` chains

## Install

[Plugin Marketplace](https://plugins.jetbrains.com/plugin/25233-react-css-module-all)

## What This Plugin Does Not Do

- Does **not** transform class names to camelCase — hyphenated names are preserved and navigable
- Does **not** handle `:global` selectors — those are treated as plain CSS

## Origin

- Forked from [jimkyndemeyer/react-css-modules-intellij-plugin](https://github.com/jimkyndemeyer/react-css-modules-intellij-plugin)
- Built with the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

## Related JetBrains Issues

- [WEB-38105 — How can I close the feature about Camel-case support for CSS Modules](https://youtrack.jetbrains.com/issue/WEB-38105)
- [WEB-41304 — CSS Modules can't navigate to class with a hyphen](https://youtrack.jetbrains.com/issue/WEB-41304)
