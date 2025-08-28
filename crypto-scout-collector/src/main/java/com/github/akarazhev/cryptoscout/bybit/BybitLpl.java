package com.github.akarazhev.cryptoscout.bybit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing the Launch Pool data in the crypto_scout schema.
 * This table is configured as a TimescaleDB hypertable for time-series data optimization.
 */
@Entity
@Table(name = "bybit_lpl", schema = "crypto_scout")
public class BybitLpl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_coin", nullable = false, length = 50)
    private String returnCoin;

    @Column(name = "return_coin_icon", nullable = false)
    private String returnCoinIcon;

    @Lob
    @Column(name = "description", nullable = false)
    private String desc;

    @Column(name = "website", nullable = false)
    private String website;

    @Column(name = "whitepaper", nullable = false)
    private String whitepaper;

    @Column(name = "rules", nullable = false)
    private String rules;

    @Column(name = "stake_begin_time", nullable = false)
    private Instant stakeBeginTime;

    @Column(name = "stake_end_time", nullable = false)
    private Instant stakeEndTime;

    @Column(name = "trade_begin_time")
    private Instant tradeBeginTime;

    /**
     * Default constructor required by JPA
     */
    public BybitLpl() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getReturnCoin() {
        return returnCoin;
    }

    public void setReturnCoin(final String returnCoin) {
        this.returnCoin = returnCoin;
    }

    public String getReturnCoinIcon() {
        return returnCoinIcon;
    }

    public void setReturnCoinIcon(final String returnCoinIcon) {
        this.returnCoinIcon = returnCoinIcon;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getWhitepaper() {
        return whitepaper;
    }

    public void setWhitepaper(final String whitepaper) {
        this.whitepaper = whitepaper;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(final String rules) {
        this.rules = rules;
    }

    public Instant getStakeBeginTime() {
        return stakeBeginTime;
    }

    public void setStakeBeginTime(final Instant stakeBeginTime) {
        this.stakeBeginTime = stakeBeginTime;
    }

    public Instant getStakeEndTime() {
        return stakeEndTime;
    }

    public void setStakeEndTime(final Instant stakeEndTime) {
        this.stakeEndTime = stakeEndTime;
    }

    public Instant getTradeBeginTime() {
        return tradeBeginTime;
    }

    public void setTradeBeginTime(final Instant tradeBeginTime) {
        this.tradeBeginTime = tradeBeginTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var bybitLpl = (BybitLpl) o;
        return Objects.equals(id, bybitLpl.id) &&
                Objects.equals(returnCoin, bybitLpl.returnCoin) &&
                Objects.equals(returnCoinIcon, bybitLpl.returnCoinIcon) &&
                Objects.equals(desc, bybitLpl.desc) &&
                Objects.equals(website, bybitLpl.website) &&
                Objects.equals(whitepaper, bybitLpl.whitepaper) &&
                Objects.equals(rules, bybitLpl.rules) &&
                Objects.equals(stakeBeginTime, bybitLpl.stakeBeginTime) &&
                Objects.equals(stakeEndTime, bybitLpl.stakeEndTime) &&
                Objects.equals(tradeBeginTime, bybitLpl.tradeBeginTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, returnCoin, returnCoinIcon, desc, website, whitepaper, 
                rules, stakeBeginTime, stakeEndTime, tradeBeginTime);
    }

    @Override
    public String toString() {
        return "BybitLpl{" +
                "id=" + id +
                ", returnCoin='" + returnCoin + '\'' +
                ", returnCoinIcon='" + returnCoinIcon + '\'' +
                ", desc='" + (desc != null ? desc.substring(0, Math.min(desc.length(), 50)) + "..." : null) + '\'' +
                ", website='" + website + '\'' +
                ", whitepaper='" + whitepaper + '\'' +
                ", rules='" + rules + '\'' +
                ", stakeBeginTime=" + stakeBeginTime +
                ", stakeEndTime=" + stakeEndTime +
                ", tradeBeginTime=" + tradeBeginTime +
                '}';
    }
}
