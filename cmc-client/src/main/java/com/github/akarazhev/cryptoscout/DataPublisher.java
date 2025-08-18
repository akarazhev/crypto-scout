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

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_ALTCOIN_SEASON_INDEX;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_BITCOIN_DOMINANCE_OVERVIEW;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_FEAR_GREED_INDEX;

@Service
final class DataPublisher implements Publisher<Payload<Map<String, Object>>> {
    private final AmqpTemplate amqpTemplate;

    public DataPublisher(final AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void publish(final Payload<Map<String, Object>> payload) {
        if (Provider.CMC.equals(payload.getProvider())) {
            if (Source.FGI.equals(payload.getSource())) {
                amqpTemplate.convertAndSend(ROUTING_METRICS_FEAR_GREED_INDEX, payload.getData());
            } else if (Source.ASI.equals(payload.getSource())) {
                amqpTemplate.convertAndSend(ROUTING_METRICS_ALTCOIN_SEASON_INDEX, payload.getData());
            } else if (Source.BDO.equals(payload.getSource())) {
                amqpTemplate.convertAndSend(ROUTING_METRICS_BITCOIN_DOMINANCE_OVERVIEW, payload.getData());
            }
        }
    }
}
