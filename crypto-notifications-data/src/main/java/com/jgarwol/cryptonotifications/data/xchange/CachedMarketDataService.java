package com.jgarwol.cryptonotifications.data.xchange;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches tickers with no expiration time until cleared manually.
 */
@Component
public class CachedMarketDataService implements MarketDataService {

    private MarketDataService marketDataService;

    private Map<CurrencyPair, Ticker> cache = new ConcurrentHashMap<>();

    @Autowired
    public CachedMarketDataService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }


    public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
        if (cache.containsKey(currencyPair)) {
            return cache.get(currencyPair);
        }
        Ticker ticker = marketDataService.getTicker(currencyPair);
        cache.put(currencyPair, ticker);
        return ticker;
    }

    public void clear() {
        cache.clear();
    }
}
