package io.github.garoluis.moviemap.external.tmdb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tmdb")
@Data
public class TmdbProperties {

    private boolean enabled = true;

    private String accessToken = "";

    private String baseUrl = "https://api.themoviedb.org/3";

    private int movieCount = 60;

    private long requestDelayMs = 250;

    public boolean hasCredentials() {
        return !accessToken.isBlank();
    }
}
