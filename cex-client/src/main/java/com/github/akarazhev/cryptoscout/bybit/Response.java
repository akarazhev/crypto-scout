package com.github.akarazhev.cryptoscout.bybit;

import java.util.List;

record Response(int retCode, String retMsg, Result result, Object retExtInfo, long time) {
    record Result(int total, List<Announcement> list) {
    }
}
