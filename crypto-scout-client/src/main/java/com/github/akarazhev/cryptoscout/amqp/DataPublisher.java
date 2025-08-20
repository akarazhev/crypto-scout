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

package com.github.akarazhev.cryptoscout.amqp;

import com.github.akarazhev.cryptoscout.Publisher;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_FEAR_GREED_INDEX;

@Service
public final class DataPublisher implements Publisher<Payload<Map<String, Object>>> {
    private final AmqpTemplate amqpTemplate;
    private final TopicExchange topicExchange;

    public DataPublisher(final AmqpTemplate amqpTemplate, final TopicExchange topicExchange) {
        this.amqpTemplate = amqpTemplate;
        this.topicExchange = topicExchange;
    }

    @Override
    public void publish(final Payload<Map<String, Object>> payload) {
        if (Provider.CMC.equals(payload.getProvider())) {
            final var data = payload.getData();
            if (Source.FGI.equals(payload.getSource())) {
                amqpTemplate.convertAndSend(topicExchange.getName(), ROUTING_METRICS_FEAR_GREED_INDEX, data);
            }
        }
    }
}
