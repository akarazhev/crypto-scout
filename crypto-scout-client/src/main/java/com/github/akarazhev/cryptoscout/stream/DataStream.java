package com.github.akarazhev.cryptoscout.stream;

import com.github.akarazhev.jcryptolib.stream.Payload;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Map;

public interface DataStream {

    Flowable<Payload<Map<String, Object>>> stream();
}
