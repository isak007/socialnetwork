package com.ftn.socialnetwork.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class RestService {

    private final RestTemplate restTemplate;
    @Value("${auth.token}")
    private String AUTH_TOKEN_FOR_MAPS_SERVICES;
    @Value("${api.key}")
    private String API_KEY;

    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Object getCityList(String cityName) {
        String url = "https://autocomplete.search.hereapi.com/v1/autocomplete";

        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // set custom header
        headers.set("x-request-source", "desktop");
        headers.set("Authorization", "Bearer "+AUTH_TOKEN_FOR_MAPS_SERVICES);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", "{q}")
                .queryParam("types", "{types}")
                .queryParam("apiKey", "{apiKey}")
                .encode()
                .toUriString();

        // create a map for post parameters
        Map<String, String> params = new HashMap<>();
        params.put("q", cityName);
        params.put("types", "city");
        params.put("apiKey", this.API_KEY);

        // build the request
        //HttpEntity<Map<String, ?>> entity = new HttpEntity<>(map, headers);

        // use `exchange` method for HTTP call
        ResponseEntity<Object> response = this.restTemplate.exchange(urlTemplate, HttpMethod.GET,entity, Object.class, params);

        // check response status code
        if (response.getStatusCode() == HttpStatus.OK){
            return response.getBody();
        } else {
            return null;
        }

    }
}