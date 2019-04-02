package com.jgarwol.cryptonotifications.data.configuration;

import org.apache.commons.lang3.NotImplementedException;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mock implementation used to imitate the real service in offline mode.
 */
@Configuration
@Profile("offline")
public class XChangeOfflineConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(XChangeOfflineConfiguration.class);

    @Bean(name = "liveMarketDataService")
    public MarketDataService createMarketDataService(){
        logger.info("creating offline xchange market data service");
        return new MarketDataService() {
            @Override
            public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
                return new Ticker.Builder()
                        .currencyPair(currencyPair)
                        .last(new BigDecimal("4700"))
                        .build();
            }

            @Override
            public List<Ticker> getTickers(Params params) throws IOException {
                throw new NotImplementedException("this method is not implemented for this class");
            }

            @Override
            public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
                throw new NotImplementedException("this method is not implemented for this class");
            }

            @Override
            public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
                throw new NotImplementedException("this method is not implemented for this class");
            }
        };
    }
}
