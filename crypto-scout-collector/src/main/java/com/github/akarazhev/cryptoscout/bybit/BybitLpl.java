package com.github.akarazhev.cryptoscout.bybit;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity representing the Launch Pool data in the crypto_scout schema.
 * This table is configured as a TimescaleDB hypertable for time-series data optimization.
 */
@Entity
@Table(name = "bybit_lpl", schema = "crypto_scout")
public class BybitLpl {
}
