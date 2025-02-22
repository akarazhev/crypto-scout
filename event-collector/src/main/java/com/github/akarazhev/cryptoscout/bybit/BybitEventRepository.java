package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;

interface BybitEventRepository extends CrudRepository<BybitEvent, Long> {

    boolean existsByTitle(final String title);
}
