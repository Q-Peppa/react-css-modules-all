# react-css-modules-all

![Downloads](https://img.shields.io/jetbrains/plugin/d/react.css.module.all)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/react.css.module.all)
![Version](https://img.shields.io/jetbrains/plugin/v/react.css.module.all)

support css module in react code completion, navigation, quick documentation.

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
2. support parents selector( & ) in scss/less file .
3. support navigation for hyphen to class.
4. auto remove some class name in `:global` tag, because it's global style.

### What's These Plugins Can't Do?

1. No enough test case


### SnapShot

![1.jpg](src%2Fmain%2Fresources%2Fpic%2F1.jpg)
![2.jpg](src%2Fmain%2Fresources%2Fpic%2F2.jpg)
![3.jpg](src%2Fmain%2Fresources%2Fpic%2F3.jpg)
![4.jpg](src%2Fmain%2Fresources%2Fpic%2F4.png)
![5.jpg](src%2Fmain%2Fresources%2Fpic%2F5.png)
