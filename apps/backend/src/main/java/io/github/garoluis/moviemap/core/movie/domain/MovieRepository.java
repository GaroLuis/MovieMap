package io.github.garoluis.moviemap.core.movie.domain;

import java.util.List;

public interface MovieRepository {

    List<Movie> findAll();
}
