package com.xanderhub.businessscenariosimulator.bootstrap;

import com.xanderhub.businessscenariosimulator.repo.EventRepository;
import com.xanderhub.businessscenariosimulator.repo.TaskRepository;
import com.xanderhub.businessscenariosimulator.model.*;
import com.xanderhub.businessscenariosimulator.repo.ScenarioRepository;
import com.xanderhub.businessscenariosimulator.repo.StateRepository;
import com.xanderhub.businessscenariosimulator.repo.TransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SimulatorBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger LOG = LoggerFactory.getLogger(SimulatorBootstrap.class);

    private final ScenarioRepository scenarioRepository;
    private final StateRepository stateRepository;
    private final TransitionRepository transitionRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;

    @Autowired
    public SimulatorBootstrap(ScenarioRepository scenarioRepository
            , StateRepository stateRepository
            , TransitionRepository transitionRepository
            , TaskRepository taskRepository
            , EventRepository eventRepository) {
        this.scenarioRepository = scenarioRepository;
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        LOG.info("Building demo scenario...");
        buildScenario();
    }

    private void buildScenario() {
        Scenario scenario = Scenario.builder()
                .name("RENT_ORDER_CREATED")
                .description("This scenario checks for car rental commitment after receiving rental order from customer").build().saveTo(scenarioRepository);

        State waiting_for_rental_start = State.builder()
                .type(StateType.INIT_STATE)
                .name("Wait for rental commitment")
                .description("Waits for customer to start his rent")
                .scenario(scenario)
                .build()
                .saveTo(stateRepository);
        State push_user = State.builder()
                .type(StateType.INTERMEDIATE_STATE)
                .name("PUSH notification")
                .description("Notifies customer about open rental status by PUSH notification")
                .scenario(scenario)
                .build()
                .saveTo(stateRepository);

        State sms_user = State.builder()
                .type(StateType.INTERMEDIATE_STATE)
                .name("SMS notification")
                .description("Notifies customer about open rental status by SMS")
                .scenario(scenario)
                .build()
                .saveTo(stateRepository);

        State call_user = State.builder()
                .type(StateType.INTERMEDIATE_STATE)
                .name("Call user")
                .description("Escalates this case to call center")
                .scenario(scenario)
                .build()
                .saveTo(stateRepository);

        State rent_started = State.builder()
                .type(StateType.FINAL_STATE)
                .name("Rent started")
                .description("Customer started his rent - complete this scenario")
                .scenario(scenario)
                .build()
                .saveTo(stateRepository);

        Transition start_to_finish = Transition.builder()
                .sourceState(waiting_for_rental_start).targetState(rent_started)
                .onEvent(Event.builder().name("RENT_STARTED").eventType(EventType.COMPLETE_SCENARIO).build().saveTo(eventRepository))
                .build()
                .saveTo(transitionRepository);

        Transition push_user_to_finish = Transition.builder()
                .sourceState(push_user).targetState(rent_started)
                .onEvent(start_to_finish.getOnEvent())
                .build()
                .saveTo(transitionRepository);

        Transition sms_user_to_finish = Transition.builder()
                .sourceState(sms_user).targetState(rent_started)
                .onEvent(start_to_finish.getOnEvent())
                .build()
                .saveTo(transitionRepository);

        Transition call_user_to_finish = Transition.builder()
                .sourceState(call_user).targetState(rent_started)
                .onEvent(start_to_finish.getOnEvent())
                .build()
                .saveTo(transitionRepository);

        Transition start_to_push_user = Transition.builder()
                .sourceState(waiting_for_rental_start).targetState(push_user)
                .onEvent(Event.builder().name("RENT DIDN'T START").eventType(EventType.NEXT_CASE_SCENARIO).build().saveTo(eventRepository))
                .build()
                .saveTo(transitionRepository);

        Transition push_to_sms_user = Transition.builder()
                .sourceState(push_user).targetState(sms_user)
                .onEvent(start_to_push_user.getOnEvent())
                .build()
                .saveTo(transitionRepository);

        Transition sms_to_call_user = Transition.builder()
                .sourceState(sms_user).targetState(call_user)
                .onEvent(start_to_push_user.getOnEvent())
                .build()
                .saveTo(transitionRepository);

        Transition call_user_to_call_user = Transition.builder()
                .sourceState(call_user).targetState(call_user)
                .onEvent(start_to_push_user.getOnEvent())
                .build()
                .saveTo(transitionRepository);


        waiting_for_rental_start.addTask(AwaitTask.builder().state(waiting_for_rental_start)
                .name("Waiting for user to start the rent").duration(5L)
                .eventOnTaskComplete(start_to_push_user.getOnEvent())
                .build().saveTo(taskRepository));

        push_user.addTask(AwaitTask.builder().state(push_user)
                .name("Waiting for user to respond on PUSH notification and start the rent").duration(5L)
                .eventOnTaskComplete(start_to_push_user.getOnEvent())
                .build().saveTo(taskRepository));

        sms_user.addTask(AwaitTask.builder().state(sms_user)
                .name("Waiting for user to respond on SMS notification and start the rent").duration(5L)
                .eventOnTaskComplete(start_to_push_user.getOnEvent())
                .build().saveTo(taskRepository));

        call_user.addTask(AwaitTask.builder().state(call_user)
                .name("Waiting for user to respond on CALL and start the rent").duration(5L)
                .eventOnTaskComplete(start_to_push_user.getOnEvent())
                .build().saveTo(taskRepository));
    }
}
