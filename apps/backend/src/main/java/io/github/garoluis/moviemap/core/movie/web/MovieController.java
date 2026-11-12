package io.github.garoluis.moviemap.core.movie.web;

import io.github.garoluis.moviemap.core.movie.domain.MovieRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieRepository movieRepository;

    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream().map(MovieResponse::from).toList();
    }
}
