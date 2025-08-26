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

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static class Arguments {
        private Arguments() {
            throw new UnsupportedOperationException();
        }

        public static int asDays(final String args) {
            var days = 14;
            if (!args.isEmpty()) {
                try {
                    days = Integer.parseInt(args);
                    if (days < 1) {
                        days = 14;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            return days;
        }
    }

    public static class Formatter {
        private Formatter() {
            throw new UnsupportedOperationException();
        }

        public static String format(final long eventTime) {
            // You'll need to implement this method to format the eventTime as needed
            // For example, converting the long to a readable date-time string
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(eventTime));
        }
    }
}
