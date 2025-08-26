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

package com.github.akarazhev.cryptoscout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for sensitive data.
 * These properties can be externalized and secured in production environments.
 */
@Configuration
@ConfigurationProperties(prefix = "crypto-scout")
public class SecureConfigProperties {

    private ApiKeys apiKeys = new ApiKeys();
    private RabbitMQ rabbitMQ = new RabbitMQ();

    public ApiKeys getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(ApiKeys apiKeys) {
        this.apiKeys = apiKeys;
    }

    public RabbitMQ getRabbitMQ() {
        return rabbitMQ;
    }

    public void setRabbitMQ(RabbitMQ rabbitMQ) {
        this.rabbitMQ = rabbitMQ;
    }

    /**
     * API keys configuration.
     */
    public static class ApiKeys {
        private String coinMarketCap;
        private String bybit;

        public String getCoinMarketCap() {
            return coinMarketCap;
        }

        public void setCoinMarketCap(String coinMarketCap) {
            this.coinMarketCap = coinMarketCap;
        }

        public String getBybit() {
            return bybit;
        }

        public void setBybit(String bybit) {
            this.bybit = bybit;
        }
    }

    /**
     * RabbitMQ configuration.
     */
    public static class RabbitMQ {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
