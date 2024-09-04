# react-css-modules-all

简单支持了 react 项目中的 scss 和 css module 的使用

### 为什么不使用WebStorm自带的功能

因为自带的功能会自动把 hyphen case 转为 camelCase , 参考下面的链接 : 
- https://youtrack.jetbrains.com/issue/WEB-38105/How-can-I-close-the-feature-about-Camel-case-support-for-CSS-Modules
- https://youtrack.jetbrains.com/issue/WEB-41304/CSS-Modules-cant-navigate-to-class-with-a-hyphen


### 这个插件能做什么

1. 自动解析css name不做case变换
2. 支持scss文件的父选择器 &
3. 点击ts文件css名称可以直接跳转到对应的引用
4. 自动移除 `:global` 标签下的选择器, 因为不是css-module选择器


### 插件不能做什么

1. 鼠标悬浮的时候,出现具体的 css block 提示
2. 识别复杂的选择器
3. 还没有足够多的测试用例


### 使用截图

![1.jpg](src%2Fmain%2Fresources%2Fpic%2F1.jpg)
![2.jpg](src%2Fmain%2Fresources%2Fpic%2F2.jpg)
![3.jpg](src%2Fmain%2Fresources%2Fpic%2F3.jpg)
