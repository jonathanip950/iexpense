package com.jebsen.iexpense.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vendor")
public class UrlConfig {
    private String url;
    private String fetchUrl;
}
