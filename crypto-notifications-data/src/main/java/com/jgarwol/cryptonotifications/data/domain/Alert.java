package com.jgarwol.cryptonotifications.data.domain;


import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Comparator.comparing;

public class Alert implements Comparable<Alert>{

    private String pair;
    private BigDecimal limit;

    public Alert() {
    }

    public Alert(String pair, BigDecimal limit) {
        this.pair = pair;
        this.limit = limit;
    }

    public String getPair() {
        return pair;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alert)) return false;
        Alert that = (Alert) o;
        return Objects.equals(pair, that.pair) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, limit);
    }

    @Override
    public int compareTo(Alert o) {
        return comparing(Alert::getPair)
                .thenComparing(Alert::getLimit)
                .compare(this, o);
    }
}
