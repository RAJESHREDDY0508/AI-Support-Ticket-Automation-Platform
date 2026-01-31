package com.ticketplatform.gateway.controller;

import com.ticketplatform.gateway.config.GatewayRouteProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * Simple REST forwarding gateway. Forwards requests matching configured route path prefixes
 * to the corresponding backend services.
 */
@RestController
@RequestMapping("/api")
public class GatewayProxyController {

    private final GatewayRouteProperties routeProperties;
    private final WebClient webClient;

    public GatewayProxyController(GatewayRouteProperties routeProperties, WebClient webClient) {
        this.routeProperties = routeProperties;
        this.webClient = webClient;
    }

    @RequestMapping(value = "/tickets/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<?> forwardToTicketService(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        String path = extractPathAfterPrefix(request.getRequestURI(), "/api/tickets");
        return forward(request, "ticket-service", path, body);
    }

    private ResponseEntity<?> forward(HttpServletRequest request, String routeName, String path, byte[] body) {
        GatewayRouteProperties.Route route = routeProperties.getRoutes().get(routeName);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        String targetUrl = route.baseUrl().replaceAll("/$", "") + path;
        if (request.getQueryString() != null) {
            targetUrl += "?" + request.getQueryString();
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        WebClient.RequestBodySpec spec = webClient
                .method(method)
                .uri(targetUrl)
                .headers(h -> {
                    copyHeaders(request, h);
                    String correlationId = request.getHeader("Correlation-ID");
                    if ((correlationId == null || correlationId.isBlank()) && MDC.get("correlationId") != null) {
                        h.set("Correlation-ID", MDC.get("correlationId"));
                    }
                });

        WebClient.ResponseSpec responseSpec;
        if (body != null && body.length > 0 && hasRequestBody(method)) {
            responseSpec = spec.bodyValue(body).retrieve();
        } else {
            responseSpec = spec.retrieve();
        }

        Mono<ResponseEntity<byte[]>> result = responseSpec.toEntity(byte[].class);
        return result.block();
    }

    private String extractPathAfterPrefix(String uri, String prefix) {
        String path = uri.startsWith(prefix) ? uri.substring(prefix.length()) : uri;
        return path.isEmpty() ? "" : (path.startsWith("/") ? path : "/" + path);
    }

    private boolean hasRequestBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    private void copyHeaders(HttpServletRequest request, HttpHeaders target) {
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String name : headerNames) {
            if (shouldForwardHeader(name)) {
                target.addAll(name, Collections.list(request.getHeaders(name)));
            }
        }
    }

    private boolean shouldForwardHeader(String name) {
        String lower = name.toLowerCase();
        return !lower.equals("host") && !lower.equals("connection") && !lower.equals("content-length");
    }
}
