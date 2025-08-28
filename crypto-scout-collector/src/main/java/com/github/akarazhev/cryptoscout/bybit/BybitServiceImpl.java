package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.CS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DATA;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.HIGH_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LAST_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LOW_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PREV_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PRICE_24H_PCNT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.SYMBOL;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TURNOVER_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TYPE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.USD_INDEX_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.VOLUME_24H;

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
                bybitLplRepository.save(getBybitLpl(payload.getData()));
            } else if (Source.WS.equals(source)) {
                bybitTickerRepository.save(getBybitTicker(payload.getData()));
            }
        }
    }

    private BybitLpl getBybitLpl(final Map<String, Object> data) {
        // TODO: implement
        return null;
    }

    private BybitTicker getBybitTicker(final Map<String, Object> data) {
        final var ticker = new BybitTicker();
        // Set top-level fields
        if (data.containsKey(TOPIC)) {
            ticker.setTopic((String) data.get(TOPIC));
        }
        
        if (data.containsKey(TS)) {
            ticker.setTimestamp(Instant.ofEpochMilli((Long) data.get(TS)));
        }
        
        if (data.containsKey(TYPE)) {
            ticker.setType((String) data.get(TYPE));
        }
        
        if (data.containsKey(CS)) {
            ticker.setCs((Long) data.get(CS));
        }
        // Process nested data object
        if (data.containsKey(DATA)) {
            final var tickerData = (Map<String, Object>) data.get(DATA);
            if (tickerData.containsKey(SYMBOL)) {
                ticker.setSymbol((String) tickerData.get(SYMBOL));
            }
            
            if (tickerData.containsKey(LAST_PRICE)) {
                ticker.setLastPrice(new BigDecimal((String) tickerData.get(LAST_PRICE)));
            }
            
            if (tickerData.containsKey(HIGH_PRICE_24H)) {
                ticker.setHighPrice24h(new BigDecimal((String) tickerData.get(HIGH_PRICE_24H)));
            }
            
            if (tickerData.containsKey(LOW_PRICE_24H)) {
                ticker.setLowPrice24h(new BigDecimal((String) tickerData.get(LOW_PRICE_24H)));
            }
            
            if (tickerData.containsKey(PREV_PRICE_24H)) {
                ticker.setPrevPrice24h(new BigDecimal((String) tickerData.get(PREV_PRICE_24H)));
            }
            
            if (tickerData.containsKey(VOLUME_24H)) {
                ticker.setVolume24h(new BigDecimal((String) tickerData.get(VOLUME_24H)));
            }
            
            if (tickerData.containsKey(TURNOVER_24H)) {
                ticker.setTurnover24h(new BigDecimal((String) tickerData.get(TURNOVER_24H)));
            }
            
            if (tickerData.containsKey(PRICE_24H_PCNT)) {
                ticker.setPrice24hPcnt(new BigDecimal((String) tickerData.get(PRICE_24H_PCNT)));
            }
            
            if (tickerData.containsKey(USD_INDEX_PRICE)) {
                ticker.setUsdIndexPrice(new BigDecimal((String) tickerData.get(USD_INDEX_PRICE)));
            }
        }

        return ticker;
    }
}
