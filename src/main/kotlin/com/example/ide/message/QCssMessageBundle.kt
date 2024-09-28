package com.example.ide.message

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.MyBundle"

internal object QCssMessageBundle {
    private val INSTANCE = DynamicBundle(QCssMessageBundle::class.java, BUNDLE)


    @JvmStatic
    fun message(
        key: @PropertyKey(resourceBundle = BUNDLE) String,
        vararg params: Any
    ) = INSTANCE.getMessage(key, *params)
}