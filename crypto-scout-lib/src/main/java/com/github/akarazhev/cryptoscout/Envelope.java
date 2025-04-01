package com.github.akarazhev.cryptoscout;

public record Envelope<T>(int current, int total, T data) {
}
