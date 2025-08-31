package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Bybit Spot Tickers ETH/USDT data.
 */
@Repository
public interface BybitSpotTickersEthUsdtRepository extends CrudRepository<BybitSpotTickersEthUsdt, Long> {
}
