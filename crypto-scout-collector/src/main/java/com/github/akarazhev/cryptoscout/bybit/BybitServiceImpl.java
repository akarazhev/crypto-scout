package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
class BybitServiceImpl implements BybitService {
    private final BybitTickerRepository bybitTickerRepository;
    private final BybitLplRepository bybitLplRepository;

    public BybitServiceImpl(final BybitTickerRepository bybitTickerRepository, final BybitLplRepository bybitLplRepository) {
        this.bybitTickerRepository = bybitTickerRepository;
        this.bybitLplRepository = bybitLplRepository;
    }

    @Override
    public void save(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        if (Provider.BYBIT.equals(provider)) {
            if (Source.LPL.equals(source)) {
                bybitLplRepository.saveAll(getBybitLpls(payload.getData()));
            } else if (Source.WS.equals(source)) {
                bybitTickerRepository.saveAll(getBybitTickers(payload.getData()));
            }
        }
    }

    private Collection<BybitLpl> getBybitLpls(final Map<String, Object> data) {
        // TODO: implement
        return Collections.emptyList();
    }

    private Collection<BybitTicker> getBybitTickers(final Map<String, Object> data) {
        // TODO: implement
        return Collections.emptyList();
    }
}
