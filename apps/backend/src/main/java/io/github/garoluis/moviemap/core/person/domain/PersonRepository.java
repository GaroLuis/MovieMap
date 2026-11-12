package io.github.garoluis.moviemap.core.person.domain;

import java.util.List;

public interface PersonRepository {

    List<Person> findAll();
}
