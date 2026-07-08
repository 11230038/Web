package com.example.end.agent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String baseUrl;

    private String apiKey;

    private String model;
}
