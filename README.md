# react-css-modules-all

support css module in react code completion

### Origin
- fork by https://github.com/jimkyndemeyer/react-css-modules-intellij-plugin/tree/master
- template by  https://github.com/JetBrains/intellij-platform-plugin-template

### Install Plugin

Plugin Home (https://plugins.jetbrains.com/plugin/25233-react-css-module-all/versions/stable/599113)

### Why Not Use WebStorm Based Function

case it will be transformed css name case to camelCase

- https://youtrack.jetbrains.com/issue/WEB-38105/How-can-I-close-the-feature-about-Camel-case-support-for-CSS-Modules
- https://youtrack.jetbrains.com/issue/WEB-41304/CSS-Modules-cant-navigate-to-class-with-a-hyphen


### What's These Plugins Do? 

1. parse css name but not transform case .
2. support parents selector( & ) in scss file .
3. support click classname link to css file.
4. auto remove some classname in `:global` tag, because it's global style.

### What's These Plugins Can't Do?

1. No enough test case


### SnapShot

![1.jpg](src%2Fmain%2Fresources%2Fpic%2F1.jpg)
![2.jpg](src%2Fmain%2Fresources%2Fpic%2F2.jpg)
![3.jpg](src%2Fmain%2Fresources%2Fpic%2F3.jpg)
![4.jpg](src%2Fmain%2Fresources%2Fpic%2F4.png)
![5.jpg](src%2Fmain%2Fresources%2Fpic%2F5.png)

### Statement

This plugin not guaranteed all function work

### Already Issue

1. can't identify .foo > &-bar#id , "&-bar#id" with another id selector'
2. com.example.ide.css.QCssModuleParseUtil.parseCssSelectorFormFile use too long time and may cause StandaloneCoroutine error
