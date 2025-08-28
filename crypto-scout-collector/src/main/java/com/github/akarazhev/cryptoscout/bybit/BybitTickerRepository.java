package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;

interface BybitTickerRepository extends CrudRepository<BybitTicker, Long> {
}
