package com.jgarwol.cryptonotifications.rest.controller;


import com.jgarwol.cryptonotifications.data.domain.AlertNotification;
import com.jgarwol.cryptonotifications.data.processor.AlertProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.Session;
import java.util.concurrent.Flow;

@Controller
public class AlertsNotificationController implements Flow.Subscriber<AlertNotification> {


    Logger logger = LoggerFactory.getLogger(AlertsNotificationController.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    AlertProcessor alertProcessor;

    @PostConstruct
    public void doSubscribe() {
        alertProcessor.subscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(AlertNotification alertNotification) {
        template.convertAndSend("/topic/alerts", alertNotification);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }


    @OnClose
    public void onClose(Session userSession) {
    }

}