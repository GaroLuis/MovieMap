package io.github.garoluis.moviemap.core.movie.data;

import io.github.garoluis.moviemap.core.movie.domain.Movie;
import io.github.garoluis.moviemap.core.movie.domain.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MovieNodeRepository implements MovieRepository {

    private final MovieNodeStore store;

    @Override
    public List<Movie> findAll() {
        return store.findAll().stream().map(MovieNode::toDomain).toList();
    }
}
