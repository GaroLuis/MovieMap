package io.github.garoluis.moviemap.core.genre.data;

import io.github.garoluis.moviemap.core.genre.domain.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreNode {

    @Id
    private int id;

    private String name;

    public GenreNode(String name) {
        this.name = name;
    }

    public static GenreNode from(Genre genre) {
        return new GenreNode(genre.getId(), genre.getName());
    }

    public Genre toDomain() {
        return new Genre(id, name);
    }
}
