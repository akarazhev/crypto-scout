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

package com.github.akarazhev.cryptoscout;

import java.util.Objects;

import static com.github.akarazhev.cryptoscout.Utils.Formatter.format;

public record Event(Platform platform, long eventTime, String type, String title, String description, String url) {
    public enum Platform {
        BYBIT;

        @Override
        public String toString() {
            if (name().isEmpty()) {
                return name();
            }

            return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var event = (Event) o;
        return eventTime == event.eventTime &&
                Objects.equals(url, event.url) &&
                Objects.equals(type, event.type) &&
                Objects.equals(title, event.title) &&
                platform == event.platform &&
                Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, eventTime, type, title, description, url);
    }

    @Override
    public String toString() {
        return String.format("""
        🌐 Platform: #%s
        
        📅 Event: %s
        🕒 Time: %s
        🏷️ Type: #%s
        
        📝 Description: %s
        
        🔗 More info: %s
        """,
                platform,
                title,
                format(eventTime),
                type,
                description,
                url
        );
    }
}
