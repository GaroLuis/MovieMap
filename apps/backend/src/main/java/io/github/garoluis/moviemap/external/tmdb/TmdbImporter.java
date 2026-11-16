package io.github.garoluis.moviemap.external.tmdb;

import io.github.garoluis.moviemap.external.tmdb.config.TmdbProperties;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbGenre;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbMovieDetails;
import io.github.garoluis.moviemap.external.tmdb.dto.TmdbPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmdbImporter implements ApplicationRunner {

    private static final int PAGE_SIZE = 20;

    private static final String UPSERT_GENRE = """
            UNWIND $rows AS row
            MERGE (g:Genre {id: row.id})
            SET g.name = row.name
            """;

    private static final String UPSERT_PERSON = """
            UNWIND $rows AS row
            MERGE (p:Person {id: row.id})
            SET p.name = row.name
            """;

    private static final String UPSERT_MOVIE = """
            UNWIND $rows AS row
            MERGE (m:Movie {id: row.id})
            SET m.title = row.title, m.rating = row.rating, m.releaseDate = row.releaseDate
            """;

    private static final String LINK_GENRE = """
            UNWIND $rows AS row
            MATCH (m:Movie {id: row.movieId}), (g:Genre {id: row.genreId})
            MERGE (m)-[:HAS_GENRE]->(g)
            """;

    private static final String LINK_CAST = """
            UNWIND $rows AS row
            MATCH (p:Person {id: row.personId}), (m:Movie {id: row.movieId})
            MERGE (p)-[:ACTED_IN]->(m)
            """;

    private static final String LINK_DIRECTOR = """
            UNWIND $rows AS row
            MATCH (p:Person {id: row.personId}), (m:Movie {id: row.movieId})
            MERGE (p)-[:DIRECTED]->(m)
            """;

    private final TmdbProperties properties;
    private final TmdbClient tmdbClient;
    private final Neo4jClient neo4jClient;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("TMDB import disabled");
            return;
        }
        if (!properties.hasCredentials()) {
            log.warn("TMDB import skipped: set TMDB_ACCESS_TOKEN to load movie data");
            return;
        }
        try {
            importData();
        } catch (Exception e) {
            log.error("TMDB import failed: {}", e.getMessage(), e);
        }
    }

    private void importData() {
        int movieCount = Math.max(1, properties.getMovieCount());
        int pages = (movieCount + PAGE_SIZE - 1) / PAGE_SIZE;

        List<TmdbGenre> genres = tmdbClient.getGenres();

        List<Integer> movieIds = new ArrayList<>();
        for (int page = 1; page <= pages && movieIds.size() < movieCount; page++) {
            movieIds.addAll(tmdbClient.getPopularMovieIds(page));
        }
        movieIds = movieIds.stream().distinct().limit(movieCount).toList();

        List<TmdbMovieDetails> movies = new ArrayList<>();
        for (int movieId : movieIds) {
            movies.add(tmdbClient.getMovieDetails(movieId));
            pause();
        }

        writeAll(genres, movies);

        log.info("TMDB import finished: {} genres, {} people, {} movies",
                genres.size(), countPeople(movies).size(), movies.size());
    }

    private void writeAll(List<TmdbGenre> genres, List<TmdbMovieDetails> movies) {
        Map<Integer, String> genreNames = new LinkedHashMap<>();
        genres.forEach(genre -> genreNames.put(genre.id(), genre.name()));

        Map<Integer, String> people = new LinkedHashMap<>();
        for (TmdbMovieDetails movie : movies) {
            movie.genres().forEach(genre -> genreNames.putIfAbsent(genre.id(), genre.name()));
            movie.cast().forEach(person -> people.putIfAbsent(person.id(), person.name()));
            movie.directors().forEach(person -> people.putIfAbsent(person.id(), person.name()));
        }

        List<Map<String, Object>> movieRows = new ArrayList<>();
        List<Map<String, Object>> genreLinks = new ArrayList<>();
        List<Map<String, Object>> castLinks = new ArrayList<>();
        List<Map<String, Object>> directorLinks = new ArrayList<>();

        for (TmdbMovieDetails movie : movies) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", movie.id());
            row.put("title", movie.title());
            row.put("rating", movie.rating());
            row.put("releaseDate", movie.releaseDate());
            movieRows.add(row);

            for (TmdbGenre genre : movie.genres()) {
                genreLinks.add(Map.of("movieId", movie.id(), "genreId", genre.id()));
            }
            for (TmdbPerson person : movie.cast()) {
                castLinks.add(Map.of("movieId", movie.id(), "personId", person.id()));
            }
            for (TmdbPerson person : movie.directors()) {
                directorLinks.add(Map.of("movieId", movie.id(), "personId", person.id()));
            }
        }

        run(UPSERT_GENRE, toRows(genreNames));
        run(UPSERT_PERSON, toRows(people));
        run(UPSERT_MOVIE, movieRows);
        run(LINK_GENRE, genreLinks);
        run(LINK_CAST, castLinks);
        run(LINK_DIRECTOR, directorLinks);
    }

    private static List<Map<String, Object>> toRows(Map<Integer, String> names) {
        List<Map<String, Object>> rows = new ArrayList<>();
        names.forEach((id, name) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", id);
            row.put("name", name);
            rows.add(row);
        });
        return rows;
    }

    private static Set<Integer> countPeople(List<TmdbMovieDetails> movies) {
        Set<Integer> people = new HashSet<>();
        movies.forEach(movie -> {
            movie.cast().forEach(person -> people.add(person.id()));
            movie.directors().forEach(person -> people.add(person.id()));
        });
        return people;
    }

    private void run(String cypher, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return;
        }
        neo4jClient.query(cypher).bind(rows).to("rows").run();
    }

    private void pause() {
        try {
            Thread.sleep(properties.getRequestDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("TMDB import interrupted", e);
        }
    }
}
