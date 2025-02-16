package com.github.akarazhev.cryptoscout.bybit;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class BybitEvent {
    @Id
    private Long id;

    public BybitEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
