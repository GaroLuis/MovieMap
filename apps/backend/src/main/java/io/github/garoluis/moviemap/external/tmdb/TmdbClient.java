package io.github.garoluis.moviemap.external.tmdb;

import io.github.garoluis.moviemap.external.tmdb.config.TmdbProperties;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbGenre;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbMovieDetails;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbPerson;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TmdbClient {

    private static final int MAX_ATTEMPTS = 3;

    private final TmdbProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<TmdbGenre> getGenres() {
        JsonNode root = get("/genre/movie/list", Map.of());
        List<TmdbGenre> genres = new ArrayList<>();
        root.path("genres").forEach(genre ->
                genres.add(new TmdbGenre(genre.path("id").asInt(), genre.path("name").asString())));
        return genres;
    }

    public List<Integer> getPopularMovieIds(int page) {
        JsonNode root = get("/movie/popular", Map.of("page", String.valueOf(page)));
        List<Integer> ids = new ArrayList<>();
        root.path("results").forEach(movie -> ids.add(movie.path("id").asInt()));
        return ids;
    }

    public TmdbMovieDetails getMovieDetails(int movieId) {
        JsonNode root = get("/movie/" + movieId, Map.of("append_to_response", "credits"));

        List<TmdbGenre> genres = new ArrayList<>();
        root.path("genres").forEach(genre ->
                genres.add(new TmdbGenre(genre.path("id").asInt(), genre.path("name").asString())));

        List<TmdbPerson> cast = new ArrayList<>();
        root.path("credits").path("cast").forEach(person ->
                cast.add(new TmdbPerson(person.path("id").asInt(), person.path("name").asString())));

        List<TmdbPerson> directors = new ArrayList<>();
        root.path("credits").path("crew").forEach(person -> {
            if ("Director".equals(person.path("job").asString())) {
                directors.add(new TmdbPerson(person.path("id").asInt(), person.path("name").asString()));
            }
        });

        return new TmdbMovieDetails(
                root.path("id").asInt(),
                root.path("title").asString(),
                root.path("vote_average").floatValue(),
                parseDate(root.path("release_date").asString(null)),
                genres,
                cast,
                directors);
    }

    private JsonNode get(String path, Map<String, String> query) {
        URI uri = buildUri(path, query);
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .GET();
                if (!properties.getAccessToken().isBlank()) {
                    builder.header("Authorization", "Bearer " + properties.getAccessToken());
                }
                HttpResponse<String> response =
                        httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status == 200) {
                    return objectMapper.readTree(response.body());
                }
                if ((status == 429 || status >= 500) && attempt < MAX_ATTEMPTS) {
                    sleep(attempt * 1000L);
                    continue;
                }
                throw new IllegalStateException(
                        "TMDB request failed (" + status + ") for " + uri + ": " + response.body());
            } catch (IOException e) {
                if (attempt < MAX_ATTEMPTS) {
                    sleep(attempt * 1000L);
                    continue;
                }
                throw new IllegalStateException("TMDB request failed for " + uri, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("TMDB request interrupted", e);
            }
        }
    }

    private URI buildUri(String path, Map<String, String> query) {
        StringBuilder builder = new StringBuilder(properties.getBaseUrl()).append(path).append('?');

        query.forEach((key, value) -> builder
                .append('&')
                .append(encode(key))
                .append('=')
                .append(encode(value)));
        return URI.create(builder.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("TMDB request interrupted", e);
        }
    }
}
