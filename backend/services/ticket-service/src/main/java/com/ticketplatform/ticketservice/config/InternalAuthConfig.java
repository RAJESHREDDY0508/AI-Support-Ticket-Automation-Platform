package com.ticketplatform.ticketservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.internal")
@Validated
public class InternalAuthConfig {

    /**
     * Shared token for internal service-to-service calls (e.g. AI worker).
     * If empty, internal endpoints are disabled.
     */
    private String token = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token != null ? token : "";
    }

    public boolean isTokenValid(String provided) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return token.equals(provided);
    }
}
