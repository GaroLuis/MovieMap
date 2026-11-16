package io.github.garoluis.moviemap.external.tmdb.dto;

import java.time.LocalDate;
import java.util.List;

public record TmdbMovieDetails(
        int id,
        String title,
        float rating,
        LocalDate releaseDate,
        List<TmdbGenre> genres,
        List<TmdbPerson> cast,
        List<TmdbPerson> directors
) {
}
