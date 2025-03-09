package com.github.akarazhev.cryptoscout;

public interface Subscriber<T> {

    void subscribe(final T object);
}
