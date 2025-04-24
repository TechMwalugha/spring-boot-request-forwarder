package com.middleware.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/forwardRequest")
    public ResponseEntity<Object> forwardRequest(
            @RequestBody Map<String, Object> requestBody,
            @RequestParam("url") String url
    ) {
        try {
            // Extracting the request body and URL
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Preparing the request entity with the body and headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Forwarding the request to the provided URL using RestTemplate, the adding the server name and the port = "http://localhost:9066/"
            ResponseEntity<Object> response = restTemplate.exchange("http://localhost:9066/" + url, HttpMethod.POST, entity, Object.class);

            // Returning the response from the forwarded request
            return ResponseEntity.status(HttpStatus.OK).body(response.getBody());

        } catch (Exception e) {
            // Handling any exceptions that occur during the process
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while forwarding the request: " + e.getMessage());
        }
    }
}
