# react-css-modules-all

支持了scss的父选择器和跳转 

idea by https://github.com/jimkyndemeyer/react-css-modules-intellij-plugin


---

```scss
.app{
    &-red{
   }
}
```

```tsx
import  styles from "./index.module.less"

//styles[""] => will popop  app , app-red
```


## TODO
1. 鼠标hover上, 应该可以预览样式code
2. 如果通过 dot key访问, 自动转化为 JSLiteralExpression 的方式
   3. `styles.fooBar -> styles['foo-bar']`;
3. 添加测试样例