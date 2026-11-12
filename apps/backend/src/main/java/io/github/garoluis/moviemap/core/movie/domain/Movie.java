package io.github.garoluis.moviemap.core.movie.domain;

import io.github.garoluis.moviemap.core.genre.domain.Genre;
import io.github.garoluis.moviemap.core.person.domain.Person;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private int id;

    private String title;

    private float rating;

    private LocalDate releaseDate;

    private Set<Genre> genres = new HashSet<>();

    private Set<Person> cast = new HashSet<>();

    private Set<Person> directors = new HashSet<>();

    public Movie(int id, String title, float rating, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }
}
