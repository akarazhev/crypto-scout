package com.github.akarazhev.cryptoscout.bybit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing the Bybit Spot Tickers BTC/USDT data in the crypto_scout schema.
 * This table is configured as a TimescaleDB hypertable for time-series data optimization.
 */
@Entity
@Table(name = "bybit_spot_tickers_btc_usdt", schema = "crypto_scout")
public class BybitSpotTickersBtcUsdt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String topic;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "cross_sequence", nullable = false)
    private Integer cs;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(name = "last_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal lastPrice;

    @Column(name = "high_price_24h", nullable = false, precision = 20, scale = 2)
    private BigDecimal highPrice24h;

    @Column(name = "low_price_24h", nullable = false, precision = 20, scale = 2)
    private BigDecimal lowPrice24h;

    @Column(name = "prev_price_24h", nullable = false, precision = 20, scale = 2)
    private BigDecimal prevPrice24h;

    @Column(name = "volume_24h", nullable = false, precision = 20, scale = 8)
    private BigDecimal volume24h;

    @Column(name = "turnover_24h", nullable = false, precision = 20, scale = 4)
    private BigDecimal turnover24h;

    @Column(name = "price_24h_pcnt", nullable = false, precision = 10, scale = 4)
    private BigDecimal price24hPcnt;

    @Column(name = "usd_index_price", precision = 20, scale = 6)
    private BigDecimal usdIndexPrice;

    /**
     * Default constructor required by JPA
     */
    public BybitSpotTickersBtcUsdt() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Integer getCs() {
        return cs;
    }

    public void setCs(final Integer cs) {
        this.cs = cs;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(final BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getHighPrice24h() {
        return highPrice24h;
    }

    public void setHighPrice24h(final BigDecimal highPrice24h) {
        this.highPrice24h = highPrice24h;
    }

    public BigDecimal getLowPrice24h() {
        return lowPrice24h;
    }

    public void setLowPrice24h(final BigDecimal lowPrice24h) {
        this.lowPrice24h = lowPrice24h;
    }

    public BigDecimal getPrevPrice24h() {
        return prevPrice24h;
    }

    public void setPrevPrice24h(final BigDecimal prevPrice24h) {
        this.prevPrice24h = prevPrice24h;
    }

    public BigDecimal getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(final BigDecimal volume24h) {
        this.volume24h = volume24h;
    }

    public BigDecimal getTurnover24h() {
        return turnover24h;
    }

    public void setTurnover24h(final BigDecimal turnover24h) {
        this.turnover24h = turnover24h;
    }

    public BigDecimal getPrice24hPcnt() {
        return price24hPcnt;
    }

    public void setPrice24hPcnt(final BigDecimal price24hPcnt) {
        this.price24hPcnt = price24hPcnt;
    }

    public BigDecimal getUsdIndexPrice() {
        return usdIndexPrice;
    }

    public void setUsdIndexPrice(final BigDecimal usdIndexPrice) {
        this.usdIndexPrice = usdIndexPrice;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (BybitSpotTickersBtcUsdt) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(type, that.type) &&
                Objects.equals(cs, that.cs) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(lastPrice, that.lastPrice) &&
                Objects.equals(highPrice24h, that.highPrice24h) &&
                Objects.equals(lowPrice24h, that.lowPrice24h) &&
                Objects.equals(prevPrice24h, that.prevPrice24h) &&
                Objects.equals(volume24h, that.volume24h) &&
                Objects.equals(turnover24h, that.turnover24h) &&
                Objects.equals(price24hPcnt, that.price24hPcnt) &&
                Objects.equals(usdIndexPrice, that.usdIndexPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topic, timestamp, type, cs, symbol, lastPrice, highPrice24h, lowPrice24h,
                prevPrice24h, volume24h, turnover24h, price24hPcnt, usdIndexPrice);
    }

    @Override
    public String toString() {
        return "BybitSpotTickersBtcUsdt{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", cs=" + cs +
                ", symbol='" + symbol + '\'' +
                ", lastPrice=" + lastPrice +
                ", highPrice24h=" + highPrice24h +
                ", lowPrice24h=" + lowPrice24h +
                ", prevPrice24h=" + prevPrice24h +
                ", volume24h=" + volume24h +
                ", turnover24h=" + turnover24h +
                ", price24hPcnt=" + price24hPcnt +
                ", usdIndexPrice=" + usdIndexPrice +
                '}';
    }
}
