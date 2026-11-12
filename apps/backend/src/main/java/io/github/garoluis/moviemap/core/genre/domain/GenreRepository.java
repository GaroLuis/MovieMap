package io.github.garoluis.moviemap.core.genre.domain;

import java.util.List;

public interface GenreRepository {

    List<Genre> findAll();
}
