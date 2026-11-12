package io.github.garoluis.moviemap.core.movie.web;

import io.github.garoluis.moviemap.core.movie.domain.Movie;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record MovieResponse(
        String id,
        String title,
        String rating,
        LocalDate releaseDate,
        Set<Genre> genres,
        Set<Person> cast,
        Set<Person> directors
) {

    public record Genre(String id, String name) {
    }

    public record Person(String id, String name) {
    }

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getRating(),
                movie.getReleaseDate(),
                movie.getGenres().stream()
                        .map(g -> new Genre(g.getId(), g.getName()))
                        .collect(Collectors.toSet()),
                movie.getCast().stream()
                        .map(p -> new Person(p.getId(), p.getName()))
                        .collect(Collectors.toSet()),
                movie.getDirectors().stream()
                        .map(p -> new Person(p.getId(), p.getName()))
                        .collect(Collectors.toSet()));
    }
}
