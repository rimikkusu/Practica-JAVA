package com.bragari.util;

// Interfata asta descrie cum pornim o actiune in background.
// Este folosita ca paginile sa nu blocheze interfata cand se incarca date.

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
