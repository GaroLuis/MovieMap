package io.github.garoluis.moviemap.core.movie.data;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MovieNodeStore extends Neo4jRepository<MovieNode, String> {
}
