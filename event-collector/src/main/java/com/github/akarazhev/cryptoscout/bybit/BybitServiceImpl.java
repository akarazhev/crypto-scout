/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.cryptoscout.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
class BybitServiceImpl implements BybitService {
    private final BybitEventRepository repository;

    public BybitServiceImpl(final BybitEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public Optional<Long> save(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (repository.existsByTitle(event.title())) {
            return Optional.empty();
        }

        final var bybitEvent = new BybitEvent();
        bybitEvent.setType(event.type());
        bybitEvent.setTitle(event.title());
        bybitEvent.setDescription(event.description());
        bybitEvent.setUrl(event.url());
        bybitEvent.setEventTime(event.eventTime());
        bybitEvent.setCreatedAt(Instant.now());
        return Optional.of(repository.save(bybitEvent).getId());
    }

    @Override
    public List<Event> getEvents(final String type, final long eventTime) {
        return repository.findByTypeAndEventTimeAfter(type, eventTime)
                .stream()
                .map(e -> new Event(Event.Platform.BYBIT, e.getEventTime(), e.getType(), e.getTitle(),
                        e.getDescription(), e.getUrl()))
                .toList();
    }
}
