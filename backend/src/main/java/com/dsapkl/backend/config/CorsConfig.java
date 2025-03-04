package com.dsapkl.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
public class CorsConfig {

    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Google Apps Script 도메인 허용
        config.addAllowedOrigin("https://script.google.com");
        config.addAllowedOrigin("https://script.googleusercontent.com");

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        source.registerCorsConfiguration("/admin/sheets/**", config);
        return new CorsFilter(source);
    }

}
