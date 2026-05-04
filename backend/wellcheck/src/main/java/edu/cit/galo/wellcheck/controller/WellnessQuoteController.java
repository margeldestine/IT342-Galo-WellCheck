package edu.cit.galo.wellcheck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wellness")
public class WellnessQuoteController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/quotes")
    public ResponseEntity<String> getQuotes() {
        String url = "https://zenquotes.io/api/quotes";
        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }
}