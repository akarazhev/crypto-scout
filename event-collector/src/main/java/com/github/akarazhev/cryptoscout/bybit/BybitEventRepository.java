package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

interface BybitEventRepository extends CrudRepository<BybitEvent, Long> {

    boolean existsByTitle(final String title);

    Collection<BybitEvent> findByEventTimeAfter(final long eventTime);
}
