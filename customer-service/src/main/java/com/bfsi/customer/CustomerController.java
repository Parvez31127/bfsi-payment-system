package com.bfsi.customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class CustomerController {

    private final RestClient restClient = RestClient.create();

    @GetMapping("/ping-payment")
    public String pingPayment() {
        String pong = restClient.get()
                .uri("http://127.0.0.1:8082/payments/ping")
                .retrieve()
                .body(String.class);
        return "payment-service says: " + pong;
    }
}