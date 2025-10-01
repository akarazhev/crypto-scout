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

package com.github.akarazhev.cryptoscout.collector;

final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class CMC {
        private CMC() {
            throw new UnsupportedOperationException();
        }

        // Insert sql
        static final String CMC_FGI_INSERT = "INSERT INTO crypto_scout.cmc_fgi " +
                "(score, name, timestamp, btc_price, btc_volume) VALUES (?, ?, ?, ?, ?)";
        static final int CMC_FGI_SCORE = 1;
        static final int CMC_FGI_NAME = 2;
        static final int CMC_FGI_TIMESTAMP = 3;
        static final int CMC_FGI_BTC_PRICE = 4;
        static final int CMC_FGI_BTC_VOLUME = 5;
    }
}
