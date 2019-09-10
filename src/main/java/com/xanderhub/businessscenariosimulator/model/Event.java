package com.xanderhub.businessscenariosimulator.model;

import com.xanderhub.businessscenariosimulator.repo.EventRepository;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;


@Entity
@SuperBuilder
@ToString
@NoArgsConstructor
public class Event implements Storable<Event, EventRepository>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private EventType eventType;
    private String name;

    @Override
    public Event saveTo(EventRepository repository) {
        return repository.save(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (!id.equals(event.id)) return false;
        if (eventType != event.eventType) return false;
        return name.equals(event.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + eventType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
