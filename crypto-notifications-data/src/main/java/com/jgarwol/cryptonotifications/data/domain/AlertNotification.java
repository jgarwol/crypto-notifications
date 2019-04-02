package com.jgarwol.cryptonotifications.data.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.Comparator.comparing;

public class AlertNotification implements Comparable<AlertNotification> {
    private String pair;
    private BigDecimal limit;
    private BigDecimal currentRate;
    private LocalDateTime timestamp;

    public AlertNotification(String pair, BigDecimal limit, BigDecimal currentRate, LocalDateTime timestamp) {
        this.pair = pair;
        this.limit = limit;
        this.currentRate = currentRate;
        this.timestamp = timestamp;
    }

    public String getPair() {
        return pair;
    }

    public BigDecimal getLimit() {
        return limit;
    }


    public BigDecimal getCurrentRate() {
        return currentRate;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(AlertNotification o) {
        return comparing(AlertNotification::getPair)
                .thenComparing(AlertNotification::getLimit)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("pair", pair)
                .append("limit", limit)
                .append("currentRate", currentRate)
                .append("timestamp", timestamp)
                .toString();
    }
}
