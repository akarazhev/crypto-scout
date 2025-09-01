package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TRADE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TURNOVER_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.USD_INDEX_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.VOLUME_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WEBSITE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WHITE_PAPER;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_BTC_USDT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_ETH_USDT;

@Service
class BybitServiceImpl implements BybitService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitServiceImpl.class);
    private final List<Map<String, Object>> tickersBuffer = new CopyOnWriteArrayList<>();
    private final List<BybitLpl> lplBuffer = new CopyOnWriteArrayList<>();
    private final BybitSpotTickersBtcUsdtRepository bybitSpotTickersBtcUsdtRepository;
    private final BybitSpotTickersEthUsdtRepository bybitSpotTickersEthUsdtRepository;
    private final BybitLplRepository bybitLplRepository;
    @Value("${crypto-scout.bybit.batch-size:100}")
    private int batchSize;
    @Value("${crypto-scout.bybit.flush-interval-ms:5000}")
    private long flushIntervalMs;

    public BybitServiceImpl(final BybitSpotTickersBtcUsdtRepository bybitSpotTickersBtcUsdtRepository,
                            final BybitSpotTickersEthUsdtRepository bybitSpotTickersEthUsdtRepository,
                            final BybitLplRepository bybitLplRepository) {
        this.bybitSpotTickersBtcUsdtRepository = bybitSpotTickersBtcUsdtRepository;
        this.bybitSpotTickersEthUsdtRepository = bybitSpotTickersEthUsdtRepository;
        this.bybitLplRepository = bybitLplRepository;
    }

    @Override
    public void save(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        if (Provider.BYBIT.equals(provider)) {
            if (Source.LPL.equals(source)) {
                lplBuffer.add(getBybitLpl(payload.getData()));
                LOGGER.debug("Added launch pool data to buffer, current size: {}", lplBuffer.size());
                if (lplBuffer.size() >= batchSize) {
                    LOGGER.info("launch pool buffer reached batch size ({}), triggering flush", batchSize);
                    flushLplBuffer();
                }
            } else if (Source.PMST.equals(source)) {
                final var data = payload.getData();
                final var topic = (String) data.get(TOPIC);
                if (TICKERS_BTC_USDT.equals(topic) || TICKERS_ETH_USDT.equals(topic)) {
                    tickersBuffer.add(data);
                    LOGGER.debug("Added spot ticker data to buffer, current size: {}", tickersBuffer.size());
                    if (tickersBuffer.size() >= batchSize) {
                        LOGGER.info("Spot ticker buffer reached batch size ({}), triggering flush", batchSize);
                        flushTickerBuffer();
                    }
                }
            }
        }
    }

    /**
     * Scheduled method to flush buffers periodically even if they don't reach the batch size
     */
    @Scheduled(fixedDelayString = "${crypto-scout.bybit.flush-interval-ms:5000}")
    public void scheduledFlush() {
        LOGGER.debug("Running scheduled flush, spot ticker buffer size: {}, launch pool buffer size: {}",
                tickersBuffer.size(), lplBuffer.size());
        flushTickerBuffer();
        flushLplBuffer();
    }

    /**
     * Flush the ticker buffer to the database
     */
    private synchronized void flushTickerBuffer() {
        if (!tickersBuffer.isEmpty()) {
            final var btcUsdtTickers = tickersBuffer.stream()
                    .filter(ticker -> TICKERS_BTC_USDT.equals(ticker.get(TOPIC)))
                    .map(this::getBybitSpotTickersBtcUsdt)
                    .collect(Collectors.toList());
            var batchSize = btcUsdtTickers.size();
            var startTime = System.currentTimeMillis();
            LOGGER.info("Flushing {} spot ticker btc-usdt records to database", batchSize);
            bybitSpotTickersBtcUsdtRepository.saveAll(btcUsdtTickers);
            var duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Successfully saved {} spot ticker btc-usdt records in {} ms ({} records/sec)",
                    batchSize, duration, batchSize > 0 ? (batchSize * 1000L / Math.max(duration, 1)) : 0);

            final var ethUsdtTickers = tickersBuffer.stream()
                    .filter(ticker -> TICKERS_ETH_USDT.equals(ticker.get(TOPIC)))
                    .map(this::getBybitSpotTickersEthUsdt)
                    .collect(Collectors.toList());
            batchSize = ethUsdtTickers.size();
            startTime = System.currentTimeMillis();
            LOGGER.info("Flushing {} spot ticker eth-usdt records to database", batchSize);
            bybitSpotTickersEthUsdtRepository.saveAll(ethUsdtTickers);
            duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Successfully saved {} spot ticker eth-usdt records in {} ms ({} records/sec)",
                    batchSize, duration, batchSize > 0 ? (batchSize * 1000L / Math.max(duration, 1)) : 0);
        }
    }

    /**
     * Flush the launch pool buffer to the database
     */
    private synchronized void flushLplBuffer() {
        if (!lplBuffer.isEmpty()) {
            List<BybitLpl> batchToSave = new ArrayList<>(lplBuffer);
            final var batchSize = batchToSave.size();
            final var startTime = System.currentTimeMillis();

            LOGGER.info("Flushing {} launch pool records to database", batchSize);
            lplBuffer.clear();
            bybitLplRepository.saveAll(batchToSave);

            final var duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Successfully saved {} launch pool records in {} ms ({} records/sec)",
                    batchSize, duration, batchSize > 0 ? (batchSize * 1000L / Math.max(duration, 1)) : 0);
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

    private BybitSpotTickersBtcUsdt getBybitSpotTickersBtcUsdt(final Map<String, Object> data) {
        final var bybitSpotTickersBtcUsdt = new BybitSpotTickersBtcUsdt();
        // Set top-level fields
        if (data.get(TS) != null) {
            bybitSpotTickersBtcUsdt.setTimestamp(Instant.ofEpochMilli((Long) data.get(TS)));
        }

        if (data.get(CS) != null) {
            bybitSpotTickersBtcUsdt.setCs((Integer) data.get(CS));
        }
        // Process nested data object
        if (data.get(DATA) != null) {
            final var tickerData = (Map<String, Object>) data.get(DATA);
            if (tickerData.get(LAST_PRICE) != null && tickerData.get(LAST_PRICE) instanceof String lastPrice) {
                if (!lastPrice.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setLastPrice(new BigDecimal(lastPrice));
                }
            }

            if (tickerData.get(HIGH_PRICE_24H) != null && tickerData.get(HIGH_PRICE_24H) instanceof String highPrice) {
                if (!highPrice.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setHighPrice24h(new BigDecimal(highPrice));
                }
            }

            if (tickerData.get(LOW_PRICE_24H) != null && tickerData.get(LOW_PRICE_24H) instanceof String lowPrice) {
                if (!lowPrice.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setLowPrice24h(new BigDecimal(lowPrice));
                }
            }

            if (tickerData.get(PREV_PRICE_24H) != null && tickerData.get(PREV_PRICE_24H) instanceof String prevPrice) {
                if (!prevPrice.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setPrevPrice24h(new BigDecimal(prevPrice));
                }
            }

            if (tickerData.get(VOLUME_24H) != null && tickerData.get(VOLUME_24H) instanceof String volume) {
                if (!volume.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setVolume24h(new BigDecimal(volume));
                }
            }

            if (tickerData.get(TURNOVER_24H) != null && tickerData.get(TURNOVER_24H) instanceof String turnover) {
                if (!turnover.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setTurnover24h(new BigDecimal(turnover));
                }
            }

            if (tickerData.get(PRICE_24H_PCNT) != null && tickerData.get(PRICE_24H_PCNT) instanceof String pricePcnt) {
                if (!pricePcnt.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setPrice24hPcnt(new BigDecimal(pricePcnt));
                }
            }

            if (tickerData.get(USD_INDEX_PRICE) != null && tickerData.get(USD_INDEX_PRICE) instanceof String usdPrice) {
                if (!usdPrice.isEmpty()) {
                    bybitSpotTickersBtcUsdt.setUsdIndexPrice(new BigDecimal(usdPrice));
                }
            }
        }

        return bybitSpotTickersBtcUsdt;
    }

    private BybitSpotTickersEthUsdt getBybitSpotTickersEthUsdt(final Map<String, Object> data) {
        final var bybitSpotTickersEthUsdt = new BybitSpotTickersEthUsdt();
        // Set top-level fields
        if (data.get(TS) != null) {
            bybitSpotTickersEthUsdt.setTimestamp(Instant.ofEpochMilli((Long) data.get(TS)));
        }

        if (data.get(CS) != null) {
            bybitSpotTickersEthUsdt.setCs((Integer) data.get(CS));
        }
        // Process nested data object
        if (data.get(DATA) != null) {
            final var tickerData = (Map<String, Object>) data.get(DATA);
            if (tickerData.get(LAST_PRICE) != null && tickerData.get(LAST_PRICE) instanceof String lastPrice) {
                if (!lastPrice.isEmpty()) {
                    bybitSpotTickersEthUsdt.setLastPrice(new BigDecimal(lastPrice));
                }
            }

            if (tickerData.get(HIGH_PRICE_24H) != null && tickerData.get(HIGH_PRICE_24H) instanceof String highPrice) {
                if (!highPrice.isEmpty()) {
                    bybitSpotTickersEthUsdt.setHighPrice24h(new BigDecimal(highPrice));
                }
            }

            if (tickerData.get(LOW_PRICE_24H) != null && tickerData.get(LOW_PRICE_24H) instanceof String lowPrice) {
                if (!lowPrice.isEmpty()) {
                    bybitSpotTickersEthUsdt.setLowPrice24h(new BigDecimal(lowPrice));
                }
            }

            if (tickerData.get(PREV_PRICE_24H) != null && tickerData.get(PREV_PRICE_24H) instanceof String prevPrice) {
                if (!prevPrice.isEmpty()) {
                    bybitSpotTickersEthUsdt.setPrevPrice24h(new BigDecimal(prevPrice));
                }
            }

            if (tickerData.get(VOLUME_24H) != null && tickerData.get(VOLUME_24H) instanceof String volume) {
                if (!volume.isEmpty()) {
                    bybitSpotTickersEthUsdt.setVolume24h(new BigDecimal(volume));
                }
            }

            if (tickerData.get(TURNOVER_24H) != null && tickerData.get(TURNOVER_24H) instanceof String turnover) {
                if (!turnover.isEmpty()) {
                    bybitSpotTickersEthUsdt.setTurnover24h(new BigDecimal(turnover));
                }
            }

            if (tickerData.get(PRICE_24H_PCNT) != null && tickerData.get(PRICE_24H_PCNT) instanceof String pricePcnt) {
                if (!pricePcnt.isEmpty()) {
                    bybitSpotTickersEthUsdt.setPrice24hPcnt(new BigDecimal(pricePcnt));
                }
            }

            if (tickerData.get(USD_INDEX_PRICE) != null && tickerData.get(USD_INDEX_PRICE) instanceof String usdPrice) {
                if (!usdPrice.isEmpty()) {
                    bybitSpotTickersEthUsdt.setUsdIndexPrice(new BigDecimal(usdPrice));
                }
            }
        }

        return bybitSpotTickersEthUsdt;
    }
}
