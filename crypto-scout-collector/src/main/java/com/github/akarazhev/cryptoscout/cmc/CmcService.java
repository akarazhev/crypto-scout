package com.github.akarazhev.cryptoscout.cmc;

import com.github.akarazhev.jcryptolib.stream.Payload;

import java.util.Map;

public interface CmcService {

    void save(final Payload<Map<String, Object>> payload);
}
