package io.github.garoluis.moviemap.core.movie.data;

import io.github.garoluis.moviemap.core.genre.data.GenreNode;
import io.github.garoluis.moviemap.core.movie.domain.Movie;
import io.github.garoluis.moviemap.core.person.data.PersonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Node("Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieNode {

    @Id
    private int id;

    private String title;

    private float rating;

    private LocalDate releaseDate;

    @Relationship(type = "HAS_GENRE", direction = Relationship.Direction.OUTGOING)
    private Set<GenreNode> genres = new HashSet<>();

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    private Set<PersonNode> cast = new HashSet<>();

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    private Set<PersonNode> directors = new HashSet<>();

    public MovieNode(int id, String title, float rating, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public static MovieNode from(Movie movie) {
        return new MovieNode(
                movie.getId(),
                movie.getTitle(),
                movie.getRating(),
                movie.getReleaseDate(),
                movie.getGenres().stream().map(GenreNode::from).collect(Collectors.toSet()),
                movie.getCast().stream().map(PersonNode::from).collect(Collectors.toSet()),
                movie.getDirectors().stream().map(PersonNode::from).collect(Collectors.toSet()));
    }

    public Movie toDomain() {
        return new Movie(
                id,
                title,
                rating,
                releaseDate,
                genres.stream().map(GenreNode::toDomain).collect(Collectors.toCollection(HashSet::new)),
                cast.stream().map(PersonNode::toDomain).collect(Collectors.toCollection(HashSet::new)),
                directors.stream().map(PersonNode::toDomain).collect(Collectors.toCollection(HashSet::new)));
    }
}
