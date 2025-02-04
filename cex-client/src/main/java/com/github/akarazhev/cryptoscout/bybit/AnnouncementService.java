package com.github.akarazhev.cryptoscout.bybit;

import java.util.stream.Stream;

public interface AnnouncementService {

    Stream<Announcement> getAnnouncements();
}
