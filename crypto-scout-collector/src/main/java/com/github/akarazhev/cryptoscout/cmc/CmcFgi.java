package com.github.akarazhev.cryptoscout.cmc;

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
 * Entity representing the Fear & Greed Index data in the crypto_scout schema.
 * This table is configured as a TimescaleDB hypertable for time-series data optimization.
 */
@Entity
@Table(name = "cmc_fgi", schema = "crypto_scout")
public class CmcFgi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "btc_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal btcPrice;

    @Column(name = "btc_volume", nullable = false, precision = 20, scale = 2)
    private BigDecimal btcVolume;

    /**
     * Default constructor required by JPA
     */
    public CmcFgi() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(final Integer score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getBtcPrice() {
        return btcPrice;
    }

    public void setBtcPrice(final BigDecimal btcPrice) {
        this.btcPrice = btcPrice;
    }

    public BigDecimal getBtcVolume() {
        return btcVolume;
    }

    public void setBtcVolume(final BigDecimal btcVolume) {
        this.btcVolume = btcVolume;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var cmcFgi = (CmcFgi) o;
        return Objects.equals(id, cmcFgi.id) &&
                Objects.equals(score, cmcFgi.score) &&
                Objects.equals(name, cmcFgi.name) &&
                Objects.equals(timestamp, cmcFgi.timestamp) &&
                Objects.equals(btcPrice, cmcFgi.btcPrice) &&
                Objects.equals(btcVolume, cmcFgi.btcVolume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, score, name, timestamp, btcPrice, btcVolume);
    }

    @Override
    public String toString() {
        return "Fgi{" +
                "id=" + id +
                ", score=" + score +
                ", name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", btcPrice=" + btcPrice +
                ", btcVolume=" + btcVolume +
                '}';
    }
}
