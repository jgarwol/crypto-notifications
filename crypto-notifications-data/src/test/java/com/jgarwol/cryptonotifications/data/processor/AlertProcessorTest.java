package com.jgarwol.cryptonotifications.data.processor;

import com.jgarwol.cryptonotifications.data.domain.Alert;
import com.jgarwol.cryptonotifications.data.domain.AlertNotification;
import com.jgarwol.cryptonotifications.data.xchange.CachedMarketDataService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

@RunWith(MockitoJUnitRunner.class)
public class AlertProcessorTest {

    public static final String BTC_EUR = "BTC/EUR";
    public static final String BTC_USD = "BTC/USD";

    @InjectMocks
    AlertProcessor underTest = new AlertProcessor();

    @Mock
    CachedMarketDataService marketDataService;

    private final Alert alert1 = new Alert(BTC_USD, new BigDecimal(123));


    private final Alert alert2 = new Alert(BTC_USD, new BigDecimal(124));
    private final Alert alert3 = new Alert(BTC_EUR, new BigDecimal(124));


    private Ticker createTicker(CurrencyPair currencyPair, double last) {
        return new Ticker.Builder()
                .currencyPair(currencyPair)
                .last(new BigDecimal(last))
                .currencyPair(CurrencyPair.BTC_USD)
                .build();
    }

    @Test
    public void shouldReturnAddedAlerts() throws IOException {
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_USD)).thenReturn(createTicker(CurrencyPair.BTC_USD, 123.0));
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_EUR)).thenReturn(createTicker(CurrencyPair.BTC_EUR, 234.0));

        Alert alert1 = new Alert(BTC_USD, new BigDecimal(123));
        Alert alert2 = new Alert(BTC_USD, new BigDecimal(124));
        Alert alert3 = new Alert(BTC_EUR, new BigDecimal(124));
        underTest.add(alert1);
        underTest.add(alert2);
        underTest.add(alert3);

        Assertions.assertThat(underTest.getAllAlerts()).containsExactlyInAnyOrder(alert1, alert2, alert3);
    }

    @Test
    public void shouldRemoveAlerts() throws IOException {
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_USD)).thenReturn(createTicker(CurrencyPair.BTC_USD, 123.0));
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_EUR)).thenReturn(createTicker(CurrencyPair.BTC_EUR, 234.0));

        underTest.add(alert1);
        underTest.add(alert2);
        underTest.add(alert3);
        underTest.remove(alert2);

        Assertions.assertThat(underTest.getAllAlerts()).containsExactlyInAnyOrder(alert1, alert3);

    }

    @Test
    public void shouldRemoveAllAlerts() throws IOException {
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_USD)).thenReturn(createTicker(CurrencyPair.BTC_USD, 123.0));
        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_EUR)).thenReturn(createTicker(CurrencyPair.BTC_EUR, 234.0));

        underTest.add(alert1);
        underTest.add(alert2);
        underTest.add(alert3);
        underTest.removeAll();

        Assertions.assertThat(underTest.getAllAlerts()).isEmpty();

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnUnknownCurrencPair() throws IOException {

        underTest.add(alert1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnMarketDataServiceException() throws IOException {

        Mockito.when(marketDataService.getTicker(CurrencyPair.BTC_USD)).thenThrow(new IOException());
        underTest.add(alert1);
    }

    @Test
    public void shouldProduceAlertNotificationWhenRateOverLimit() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double rate = 125.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(currencyPair)).thenReturn(createTicker(currencyPair, rate));

        underTest.subscribe(subscriber);
        underTest.add(new Alert(currencyPair.toString(), new BigDecimal(limit)));
        underTest.processAlerts();

        Assertions.assertThat(subscriber.getAlertNotifications())
                .usingElementComparatorIgnoringFields("timestamp")
                .containsExactly(new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(rate), LocalDateTime.now()));

    }

    @Test
    public void shouldProduceOnlyOneAlertNotificationWhenRateAlwaysOverLimit() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double overLimit = 125.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(currencyPair))
                .thenReturn(createTicker(currencyPair, overLimit))
                .thenReturn(createTicker(currencyPair, overLimit))
                .thenReturn(createTicker(currencyPair, overLimit))
        ;

        underTest.subscribe(subscriber);
        underTest.add(new Alert(currencyPair.toString(), new BigDecimal(limit)));
        underTest.processAlerts();
        underTest.processAlerts();
        underTest.processAlerts();

        Assertions.assertThat(subscriber.getAlertNotifications())
                .usingElementComparatorIgnoringFields("timestamp")
                .containsExactly(new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(overLimit), LocalDateTime.now()));

    }



    @Test
    public void shouldProduceOneAlertWhenOverLimitAndAnotherAfterHavingGoneBelowLimit() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double overLimit1 = 125.0;
        double underLimit = 123.0;
        double overLimit2 = 126.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(Mockito.eq(currencyPair)))
                .thenReturn(
                        createTicker(currencyPair, overLimit1),
                        createTicker(currencyPair, overLimit1),
                        createTicker(currencyPair, underLimit),
                        createTicker(currencyPair, overLimit2)
                )
        ;



        underTest.subscribe(subscriber);
        underTest.add(new Alert(currencyPair.toString(), new BigDecimal(limit)));
        underTest.processAlerts();
        underTest.processAlerts();
        underTest.processAlerts();

        Assertions.assertThat(subscriber.getAlertNotifications())
                .usingElementComparatorIgnoringFields("timestamp")
                .containsExactly(
                        new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(overLimit1), LocalDateTime.now()),
                        new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(overLimit2), LocalDateTime.now())
                );

    }

    @Test
    public void shouldProduceOneAlertWhenOverLimitAndAnotherAfterHavingBeenAddedAgain() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double overLimit = 125.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(Mockito.eq(currencyPair))).thenReturn(createTicker(currencyPair, overLimit))
        ;



        underTest.subscribe(subscriber);
        Alert alert = new Alert(currencyPair.toString(), new BigDecimal(limit));
        underTest.add(alert);
        underTest.processAlerts();
        underTest.remove(alert);
        underTest.add(alert);
        underTest.processAlerts();

        Assertions.assertThat(subscriber.getAlertNotifications())
                .usingElementComparatorIgnoringFields("timestamp")
                .containsExactly(
                        new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(overLimit), LocalDateTime.now()),
                        new AlertNotification(currencyPair.toString(), new BigDecimal(limit), new BigDecimal(overLimit), LocalDateTime.now())
                );

    }

    @Test
    public void shouldKeepAlertWhenNullTickerIsReturnedDuringProcessing() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double overLimit = 125.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(Mockito.eq(currencyPair))).thenReturn(createTicker(currencyPair, overLimit), null)
        ;



        underTest.subscribe(subscriber);
        Alert alert = new Alert(currencyPair.toString(), new BigDecimal(limit));
        underTest.add(alert);
        underTest.processAlerts();


        Mockito.verify(marketDataService, Mockito.atLeast(2)).getTicker(Mockito.eq(currencyPair));
        Assertions.assertThat(subscriber.getAlertNotifications()).isEmpty();

    }

    @Test
    public void shouldNotProduceAlertNotificationWhenRateUnderLimit() throws IOException {
        CurrencyPair currencyPair = CurrencyPair.BTC_USD;
        double rate = 123.0;
        int limit = 124;

        SimpleAlertNotificationSubscriber subscriber = new SimpleAlertNotificationSubscriber();
        Mockito.when(marketDataService.getTicker(currencyPair)).thenReturn(createTicker(currencyPair, rate));

        underTest.subscribe(subscriber);
        underTest.add(new Alert(currencyPair.toString(), new BigDecimal(limit)));
        underTest.processAlerts();

        Assertions.assertThat(subscriber.getAlertNotifications()).isEmpty();

    }

    private static class SimpleAlertNotificationSubscriber implements Flow.Subscriber<AlertNotification> {
        private List<AlertNotification> alertNotifications = new ArrayList<>();

        public List<AlertNotification> getAlertNotifications() {
            return alertNotifications;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {

        }

        @Override
        public void onNext(AlertNotification alertNotification) {
            this.alertNotifications.add(alertNotification);
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {

        }
    }
}