package com.xanderhub.businessscenariosimulator.service;

import com.xanderhub.businessscenariosimulator.model.*;
import com.xanderhub.businessscenariosimulator.repo.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.object;

public class Simulator implements Consumer<reactor.bus.Event<Event>> {
    private final Logger LOG = LoggerFactory.getLogger(Simulator.class);

    private final Scenario scenario;
    private State currentState;
    private final EventBus eventBus;

    public Simulator(final Scenario scenario, final EventBus eventBus, final StateRepository stateRepository) {
        this.eventBus = eventBus;
        this.scenario = scenario;
        this.currentState = stateRepository.findByScenarioAndType(scenario, StateType.INIT_STATE);
        eventBus.on(object(this), this);
    }

    public void executeScenario(){
        LOG.info("Executing scenario: {}", scenario.getName());
        currentState.runOnSimulator(this);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void accept(reactor.bus.Event<Event> event) {
        LOG.debug("Event received: {}", event.getData());
        handleEvent(event.getData());
    }

    private void handleEvent(Event event){
        Transition transition = currentState.getTransitions().stream()
                .filter(t -> t.getOnEvent().equals(event))
                .findAny()
                .orElse(null);

        if(transition != null){
            if(currentState.getType() != StateType.FINAL_STATE) {
                currentState.cancelAllTasks();
                currentState = transition.getTargetState();
                currentState.runOnSimulator(this);
            }
        }
    }

}
