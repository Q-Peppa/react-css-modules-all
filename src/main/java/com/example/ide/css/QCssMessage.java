package com.example.ide.css;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.*;

import java.util.function.Supplier;

public final class QCssMessage {
    @NonNls
    private static final String BUNDLE = "messages.MyBundle";
    private static final DynamicBundle INSTANCE =
            new DynamicBundle(QCssMessage.class, BUNDLE);

    private QCssMessage() {}

    public static @NotNull @Nls String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            Object @NotNull ... params
    ) {
        return INSTANCE.getMessage(key, params);
    }

    public static Supplier<@Nls String> lazyMessage(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            Object @NotNull ... params
    ) {
        return INSTANCE.getLazyMessage(key, params);
    }
}