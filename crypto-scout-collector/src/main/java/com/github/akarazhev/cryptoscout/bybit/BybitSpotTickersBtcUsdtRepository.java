package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Bybit Spot Tickers BTC/USDT data.
 */
@Repository
public interface BybitSpotTickersBtcUsdtRepository extends CrudRepository<BybitSpotTickersBtcUsdt, Long> {
}
