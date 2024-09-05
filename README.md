# react-css-modules-all

simply support css module in react code completion

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

1. Show document when mouse hover.
2. parse very complex css selector.
3. No enough test case


### SnapShot

![1.jpg](src%2Fmain%2Fresources%2Fpic%2F1.jpg)
![2.jpg](src%2Fmain%2Fresources%2Fpic%2F2.jpg)
![3.jpg](src%2Fmain%2Fresources%2Fpic%2F3.jpg)


### Statement

This plugin not guaranteed all function work

### Already Issue

1. complex selector