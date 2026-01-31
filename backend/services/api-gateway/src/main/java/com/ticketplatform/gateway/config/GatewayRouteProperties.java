package com.ticketplatform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
@Validated
public class GatewayRouteProperties {

    private Map<String, Route> routes = new HashMap<>();

    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Route> routes) {
        this.routes = routes;
    }

    public record Route(String baseUrl, String pathPrefix) {}
}
