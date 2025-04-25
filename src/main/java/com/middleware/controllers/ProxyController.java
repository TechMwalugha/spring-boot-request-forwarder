package com.middleware.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BACKEND_BASE_URL = "http://localhost:9066";

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String body) {
        try {
            // Extract the original path after the context path (e.g. /adaptor/cms/card/inquiry)
            String forwardPath = request.getRequestURI();

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Add original request headers to forward
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                headers.set(headerName, request.getHeader(headerName));
            });

            // Prepare entity with body and headers
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Determine method
            HttpMethod httpMethod;
            try {
                httpMethod = HttpMethod.valueOf(request.getMethod());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body("Unsupported HTTP method: " + request.getMethod());
            }


            // Forward request to backend
            String targetUrl = BACKEND_BASE_URL + forwardPath;
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, httpMethod, entity, Object.class);

            // Return the response as it is
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while forwarding the request: " + e.getMessage());
        }
    }
}
