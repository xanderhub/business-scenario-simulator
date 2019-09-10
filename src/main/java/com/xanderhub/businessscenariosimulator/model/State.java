package com.xanderhub.businessscenariosimulator.model;

import com.xanderhub.businessscenariosimulator.repo.StateRepository;
import com.xanderhub.businessscenariosimulator.service.Simulator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
public class State implements Storable<State, StateRepository>{
    @Transient
    private final Logger LOG = LoggerFactory.getLogger(State.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private StateType type;

    @OneToMany(mappedBy = "sourceState", fetch = FetchType.EAGER)
    private final Set<Transition> transitions = new HashSet<>();

    @ManyToOne
    @JoinColumn
    private Scenario scenario;

    @OneToMany(mappedBy = "state", fetch = FetchType.EAGER)
    private final Set<Task> tasks = new HashSet<>();

    @Transient
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void addTask(Task task){
        tasks.add(task);
    }


    public void cancelAllTasks(){
        executorService.shutdownNow();
    }

    public void runOnSimulator(final Simulator simulator){
        LOG.info("STATE: {}", getName());
        for (Task task: tasks) {
            task.setSimulator(simulator);
            task.setEventBus(simulator.getEventBus());
            if(executorService.isTerminated())
                executorService = Executors.newSingleThreadExecutor();
            executorService.submit(task);
        }
    }

    @Override
    public State saveTo(StateRepository repository) {
        return repository.save(this);
    }
}
