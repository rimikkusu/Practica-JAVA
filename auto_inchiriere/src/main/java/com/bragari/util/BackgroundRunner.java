package com.bragari.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

@FunctionalInterface
public interface BackgroundRunner {
    <T> void run(Callable<T> action, Consumer<T> onSuccess, Consumer<Throwable> onError);

    default <T> void run(Callable<T> action, Consumer<T> onSuccess) {
        run(action, onSuccess, error -> {
        });
    }
}
