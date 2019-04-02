package com.jgarwol.cryptonotifications.rest.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgarwol.cryptonotifications.Application;
import com.jgarwol.cryptonotifications.data.domain.Alert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test to run the application.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class AlertControllerTestIT {

    public static final String BTC_USD = "BTC/USD";
    @Autowired
    private WebApplicationContext context;
    @Autowired
    ObjectMapper objectMapper;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        deleteAllAlerts();
        verifyNoAlerts();
    }


    @Test
    public void shouldReturnAlertAfterPut() throws Exception {

        String pair = BTC_USD;
        BigDecimal limit = BigDecimal.valueOf(100);
        Alert newAlert = new Alert(pair, limit);

        putAlert(newAlert);
        getAlertsAndExpect(newAlert);
    }

    @Test
    public void shouldDeleteAlert() throws Exception {

        String pair = BTC_USD;
        BigDecimal limit = BigDecimal.valueOf(100);
        Alert newAlert = new Alert(pair, limit);

        putAlert(newAlert);
        deleteAlert(newAlert);
        verifyNoAlerts();
    }

    @Test
    public void shouldAllowMultipleAlertsPerPair() throws Exception {

        String pair = BTC_USD;
        BigDecimal limit1 = BigDecimal.valueOf(100);
        BigDecimal limit2 = BigDecimal.valueOf(200);

        Alert newAlert1 = new Alert(pair, limit1);
        Alert newAlert2 = new Alert(pair, limit2);
        putAlert(newAlert1);
        putAlert(newAlert2);
        getAlertsAndExpect(newAlert1, newAlert2);
    }

    @Test
    public void shouldDeleteAlertsForPair() throws Exception {

        this.mvc.perform(delete("/api/alert")
                .param("pair", BTC_USD)
        ).andExpect(status().isBadRequest());

    }

    @Test
    public void shouldNotAllowMultipleAlertsPerPairAndLimit() throws Exception {

        String pair = BTC_USD;
        BigDecimal limit1 = BigDecimal.valueOf(100);

        Alert newAlert1 = new Alert(pair, limit1);
        putAlert(newAlert1);
        putAlert(newAlert1);
        getAlertsAndExpect(newAlert1);
    }
    @Test
    public void shouldReturnEmptyList() throws Exception {

        getAlerts()
                .andExpect(jsonPath("$.length()").value("0"))
                .andDo(print());
        getAlertsAndExpect();
    }

    private void deleteAllAlerts() throws Exception {
        this.mvc.perform(delete("/api/alert"))
                .andExpect(status().isOk())
        ;

    }

    //added for clarity
    private void verifyNoAlerts() throws Exception {
        getAlertsAndExpect();
    }

    private ResultActions getAlertsAndExpect(Alert... expectedAlerts) throws Exception {
        return getAlerts()
                .andDo(print())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().json(alertsToJson(expectedAlerts)))
                ;
    }

    private ResultActions getAlerts() throws Exception {
        return this.mvc.perform(get("/api/alert"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                ;
    }

    private ResultActions deleteAlert(Alert alert) throws Exception {
        return this.mvc.perform(delete("/api/alert")
                .param("pair", alert.getPair())
                .param("limit", String.valueOf(alert.getLimit()))
        ).andExpect(status().isOk());
    }

    private ResultActions putAlert(Alert alert) throws Exception {
        return this.mvc.perform(put("/api/alert")
                .param("pair", alert.getPair())
                .param("limit", String.valueOf(alert.getLimit()))
        ).andExpect(status().isOk());
    }

    private String alertsToJson(Alert... t) throws JsonProcessingException {
        return objectMapper.writeValueAsString(Arrays.asList(t));
    }

    private Iterable<Alert> jsonToAlerts(String jsonResponse) throws IOException {
        return objectMapper.readValue(jsonResponse, Iterable.class);
    }
}
