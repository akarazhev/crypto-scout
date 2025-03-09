package com.github.akarazhev.cryptoscout;

public interface Publisher<T> {

    void publish(final T event);
}
