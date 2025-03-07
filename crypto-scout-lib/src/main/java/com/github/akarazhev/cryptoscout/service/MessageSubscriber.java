package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Message;

interface MessageSubscriber {

    void subscribe(final Message message);
}
