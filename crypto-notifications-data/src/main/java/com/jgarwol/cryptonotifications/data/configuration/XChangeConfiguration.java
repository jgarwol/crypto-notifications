package com.jgarwol.cryptonotifications.data.configuration;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!offline")
public class XChangeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(XChangeConfiguration.class);

    @Bean(name = "liveMarketDataService")
    public MarketDataService createMarketDataService(){
        logger.info("creating online xchange market data service");
        return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()).getMarketDataService();
    }
}
