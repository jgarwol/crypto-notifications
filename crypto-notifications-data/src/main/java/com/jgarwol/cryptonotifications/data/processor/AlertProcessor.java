package com.jgarwol.cryptonotifications.data.processor;

import com.jgarwol.cryptonotifications.data.domain.Alert;
import com.jgarwol.cryptonotifications.data.domain.AlertNotification;
import com.jgarwol.cryptonotifications.data.xchange.CachedMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Flow;

@Component
public class AlertProcessor implements Flow.Publisher<AlertNotification> {

    private static final Logger logger = LoggerFactory.getLogger(AlertProcessor.class);

    @Autowired
    private CachedMarketDataService cachedMarketDataService;

    private Set<Alert> alertsSet = new ConcurrentSkipListSet<>();
    private Set<Alert> alertsTriggered = new ConcurrentSkipListSet<>();
    private Set<Flow.Subscriber<? super AlertNotification>> subscribers = Collections.synchronizedSet(new HashSet<>());


    public void add(Alert alert) {
        verifyIfValid(new CurrencyPair(alert.getPair()));
        alertsSet.add(alert);
    }

    private void verifyIfValid(CurrencyPair currencyPair) {
        Optional<Ticker> optionalTicker = getTickerWithExceptionHandling(currencyPair);
        optionalTicker.orElseThrow(() -> new IllegalArgumentException("Currency pair "+currencyPair+" is not valid"));
    }

    public void remove(Alert alert){
        alertsSet.remove(alert);
        alertsTriggered.remove(alert);
    }

    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alertsSet);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super AlertNotification> subscriber) {
        subscribers.add(subscriber);
    }

    @Scheduled(fixedRate = 1000)
    public void processAlerts() throws IOException {
        logger.debug("Processing alerts");
        if(subscribers.isEmpty()){
            //no need to check for alarms if there are no subscribers
            logger.debug("no subscribers. ");
            return;
        }
        cachedMarketDataService.clear();
        alertsSet.forEach(this::checkForNotifications);
    }

    private void checkForNotifications(Alert alert){
        Optional<Ticker> optionalTicker = getTickerWithExceptionHandling(new CurrencyPair(alert.getPair()));
        if(!optionalTicker.isPresent()){
            return;
        }
        Ticker ticker = optionalTicker.get();
        if(ticker.getLast().compareTo(alert.getLimit()) > 0){
            if(!alertsTriggered.contains(alert)) {
                notifySubscribers(new AlertNotification(alert.getPair(), alert.getLimit(), ticker.getLast(), LocalDateTime.now()));
                alertsTriggered.add(alert);
            }
        } else {
            alertsTriggered.remove(alert);
        }
    }

    private void notifySubscribers(AlertNotification alertNotification) {
        for (Flow.Subscriber<? super AlertNotification> subscriber : subscribers) {
            subscriber.onNext(alertNotification);
        }
    }

    private Optional<Ticker> getTickerWithExceptionHandling(CurrencyPair currencyPair) {
        Ticker ticker = null;
        try {
            ticker = cachedMarketDataService.getTicker(currencyPair);
        } catch (IOException e) {
            logger.warn("Exception when querying for currency pair: "+currencyPair);
        }
        return Optional.ofNullable(ticker);
    }


    public void removeAll() {
        alertsSet.clear();
    }
}