package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.jcryptolib.stream.Payload;

import java.util.Map;

public interface BybitService {

    void save(final Payload<Map<String, Object>> payload);
}
