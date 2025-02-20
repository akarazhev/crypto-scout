package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.data.repository.CrudRepository;

interface BybitEventRepository extends CrudRepository<BybitEvent, Long> {

    boolean existsByEventTimeAndPublishTimeAndStartTimeAndEndTime(Long eventTime, Long publishTime, Long startTime,
                                                                  Long endTime);
}
