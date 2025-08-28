package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.CS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DATA;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DESC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.HIGH_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LAST_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LOW_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PREV_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PRICE_24H_PCNT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN_ICON;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RULES;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_END_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.SYMBOL;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TRADE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TURNOVER_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TYPE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.USD_INDEX_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.VOLUME_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WEBSITE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WHITE_PAPER;

@Service
class BybitServiceImpl implements BybitService {
    // Buffers for batch processing
    private final List<BybitTicker> tickerBuffer = new CopyOnWriteArrayList<>();
    private final List<BybitLpl> lplBuffer = new CopyOnWriteArrayList<>();
    private final BybitTickerRepository bybitTickerRepository;
    private final BybitLplRepository bybitLplRepository;
    @Value("${crypto-scout.bybit.batch-size:100}")
    private int batchSize;
    @Value("${crypto-scout.bybit.flush-interval-ms:5000}")
    private long flushIntervalMs;

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
                final BybitLpl lpl = getBybitLpl(payload.getData());
                lplBuffer.add(lpl);
                // Flush if buffer size reaches the threshold
                if (lplBuffer.size() >= batchSize) {
                    flushLplBuffer();
                }
            } else if (Source.WS.equals(source)) {
                final BybitTicker ticker = getBybitTicker(payload.getData());
                tickerBuffer.add(ticker);
                // Flush if buffer size reaches the threshold
                if (tickerBuffer.size() >= batchSize) {
                    flushTickerBuffer();
                }
            }
        }
    }
    
    /**
     * Scheduled method to flush buffers periodically even if they don't reach the batch size
     */
    @Scheduled(fixedDelayString = "${crypto-scout.bybit.flush-interval-ms:5000}")
    public void scheduledFlush() {
        flushTickerBuffer();
        flushLplBuffer();
    }
    
    /**
     * Flush the ticker buffer to the database
     */
    private synchronized void flushTickerBuffer() {
        if (!tickerBuffer.isEmpty()) {
            List<BybitTicker> batchToSave = new ArrayList<>(tickerBuffer);
            tickerBuffer.clear();
            bybitTickerRepository.saveAll(batchToSave);
        }
    }
    
    /**
     * Flush the LPL buffer to the database
     */
    private synchronized void flushLplBuffer() {
        if (!lplBuffer.isEmpty()) {
            List<BybitLpl> batchToSave = new ArrayList<>(lplBuffer);
            lplBuffer.clear();
            bybitLplRepository.saveAll(batchToSave);
        }
    }

    private BybitLpl getBybitLpl(final Map<String, Object> data) {
        final var bybitLpl = new BybitLpl();
        if (data.get(RETURN_COIN) != null) {
            bybitLpl.setReturnCoin((String) data.get(RETURN_COIN));
        }
        
        if (data.get(RETURN_COIN_ICON) != null) {
            bybitLpl.setReturnCoinIcon((String) data.get(RETURN_COIN_ICON));
        }
        
        if (data.get(DESC) != null) {
            bybitLpl.setDesc((String) data.get(DESC));
        }
        
        if (data.get(WEBSITE) != null) {
            bybitLpl.setWebsite((String) data.get(WEBSITE));
        }
        
        if (data.get(WHITE_PAPER) != null) {
            bybitLpl.setWhitepaper((String) data.get(WHITE_PAPER));
        }
        
        if (data.get(RULES) != null) {
            bybitLpl.setRules((String) data.get(RULES));
        }
        
        if (data.get(STAKE_BEGIN_TIME) != null) {
            bybitLpl.setStakeBeginTime(Instant.ofEpochMilli((Long) data.get(STAKE_BEGIN_TIME)));
        }
        
        if (data.get(STAKE_END_TIME) != null) {
            bybitLpl.setStakeEndTime(Instant.ofEpochMilli((Long) data.get(STAKE_END_TIME)));
        }
        
        // Trade begin time can be null
        if (data.get(TRADE_BEGIN_TIME) != null) {
            bybitLpl.setTradeBeginTime(Instant.ofEpochMilli((Long) data.get(TRADE_BEGIN_TIME)));
        } else {
            bybitLpl.setTradeBeginTime(null);
        }
        
        return bybitLpl;
    }

    private BybitTicker getBybitTicker(final Map<String, Object> data) {
        final var bybitTicker = new BybitTicker();
        // Set top-level fields
        if (data.get(TOPIC) != null) {
            bybitTicker.setTopic((String) data.get(TOPIC));
        }
        
        if (data.get(TS) != null) {
            bybitTicker.setTimestamp(Instant.ofEpochMilli((Long) data.get(TS)));
        }
        
        if (data.get(TYPE) != null) {
            bybitTicker.setType((String) data.get(TYPE));
        }
        
        if (data.get(CS) != null) {
            bybitTicker.setCs((Integer) data.get(CS));
        }
        // Process nested data object
        if (data.get(DATA) != null) {
            final var tickerData = (Map<String, Object>) data.get(DATA);
            if (tickerData.get(SYMBOL) != null) {
                bybitTicker.setSymbol((String) tickerData.get(SYMBOL));
            }
            
            if (tickerData.get(LAST_PRICE) != null) {
                bybitTicker.setLastPrice(new BigDecimal((String) tickerData.get(LAST_PRICE)));
            }
            
            if (tickerData.get(HIGH_PRICE_24H) != null) {
                bybitTicker.setHighPrice24h(new BigDecimal((String) tickerData.get(HIGH_PRICE_24H)));
            }
            
            if (tickerData.get(LOW_PRICE_24H) != null) {
                bybitTicker.setLowPrice24h(new BigDecimal((String) tickerData.get(LOW_PRICE_24H)));
            }
            
            if (tickerData.get(PREV_PRICE_24H) != null) {
                bybitTicker.setPrevPrice24h(new BigDecimal((String) tickerData.get(PREV_PRICE_24H)));
            }
            
            if (tickerData.get(VOLUME_24H) != null) {
                bybitTicker.setVolume24h(new BigDecimal((String) tickerData.get(VOLUME_24H)));
            }
            
            if (tickerData.get(TURNOVER_24H) != null) {
                bybitTicker.setTurnover24h(new BigDecimal((String) tickerData.get(TURNOVER_24H)));
            }
            
            if (tickerData.get(PRICE_24H_PCNT) != null) {
                bybitTicker.setPrice24hPcnt(new BigDecimal((String) tickerData.get(PRICE_24H_PCNT)));
            }
            
            if (tickerData.get(USD_INDEX_PRICE) != null) {
                bybitTicker.setUsdIndexPrice(new BigDecimal((String) tickerData.get(USD_INDEX_PRICE)));
            }
        }
        
        return bybitTicker;
    }
}
