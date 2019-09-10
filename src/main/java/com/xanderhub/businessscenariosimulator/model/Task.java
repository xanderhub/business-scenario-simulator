package com.xanderhub.businessscenariosimulator.model;


import com.xanderhub.businessscenariosimulator.repo.TaskRepository;
import com.xanderhub.businessscenariosimulator.service.Simulator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.bus.EventBus;

import javax.persistence.*;

import static reactor.bus.selector.Selectors.object;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class Task implements Runnable, Storable<Task, TaskRepository> {
    @Transient
    private final Logger LOG = LoggerFactory.getLogger(Task.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Transient
    private EventBus eventBus;

    @Transient
    private Simulator simulator;

    @Transient
    private Boolean isStopped;

    @ManyToOne
    @JoinColumn
    private State state;

    @OneToOne
    private Event eventOnTaskStart;

    @OneToOne
    private Event eventOnTaskComplete;

    private void eventOnStart() {
        if (eventOnTaskStart != null && !isStopped) {
            eventBus.notify(object(simulator).getObject(), reactor.bus.Event.wrap(eventOnTaskStart));
            LOG.debug("eventOnTaskStart fired - {}", eventOnTaskStart);
        }
    }

    private void eventOnComplete() {
        if (eventOnTaskComplete != null && !isStopped) {
            eventBus.notify(object(simulator).getObject(), reactor.bus.Event.wrap(eventOnTaskComplete));
            LOG.debug("eventOnTaskComplete fired - {}", eventOnTaskComplete);
        }
    }

    protected abstract void execute();

    @Override
    public void run(){
        isStopped = false;
        eventOnStart();

        if(!isStopped){
            LOG.debug("Task - {} - STARTED", getName());
            execute();
            LOG.debug("Task - {} - COMPLETED", getName());
        }

        eventOnComplete();
    }

    @Override
    public Task saveTo(TaskRepository repository) {
        return repository.save(this);
    }
}
