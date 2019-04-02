package com.jgarwol.cryptonotifications.rest.controller;


import com.jgarwol.cryptonotifications.data.domain.Alert;
import com.jgarwol.cryptonotifications.data.processor.AlertProcessor;
import com.jgarwol.cryptonotifications.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController()
public class AlertController {

    @Autowired
    AlertProcessor alertProcessor;

    @RequestMapping(method = GET, path = "/api/alert")
    public Iterable<Alert> getAlerts() {
        return alertProcessor.getAllAlerts();
    }

    @RequestMapping(method = PUT, path = "/api/alert")
    public void putAlert(@RequestParam String pair, @RequestParam String limit) {
        try {
            alertProcessor.add(new Alert(pair, new BigDecimal(limit)));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(method = DELETE, path = "/api/alert")
    public void deleteAlert(@RequestParam(required = false) String pair, @RequestParam(required = false) String limit) {
        if (pair != null && limit != null) {
            alertProcessor.remove(new Alert(pair, new BigDecimal(limit)));
        } else if (pair == null && limit == null) {
            alertProcessor.removeAll();
        } else {
            throw new BadRequestException("Provide either both pair and limit or nothing.");
        }
    }


}