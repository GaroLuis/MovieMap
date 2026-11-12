package io.github.garoluis.moviemap.core.person.data;

import io.github.garoluis.moviemap.core.person.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonNode {

    @Id
    private int id;

    private String name;

    public static PersonNode from(Person person) {
        return new PersonNode(person.getId(), person.getName());
    }

    public Person toDomain() {
        return new Person(id, name);
    }
}
