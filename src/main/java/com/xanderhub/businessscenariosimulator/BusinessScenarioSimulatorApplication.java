package com.xanderhub.businessscenariosimulator;

import com.xanderhub.businessscenariosimulator.model.*;
import com.xanderhub.businessscenariosimulator.repo.*;
import com.xanderhub.businessscenariosimulator.service.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.bus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class BusinessScenarioSimulatorApplication implements CommandLineRunner {

	private final EventBus eventBus;
	private final ScenarioRepository scenarioRepository;
	private final StateRepository stateRepository;
	private final TransitionRepository transitionRepository;
	private final TaskRepository taskRepository;
	private final EventRepository eventRepository;

	@Autowired
	public BusinessScenarioSimulatorApplication(EventBus eventBus
			, ScenarioRepository scenarioRepository
			, StateRepository stateRepository
			, TransitionRepository transitionRepository
			, TaskRepository taskRepository
			, EventRepository eventRepository) {
		this.eventBus = eventBus;
		this.scenarioRepository = scenarioRepository;
		this.stateRepository = stateRepository;
		this.transitionRepository = transitionRepository;
		this.taskRepository = taskRepository;
		this.eventRepository = eventRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(BusinessScenarioSimulatorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Scenario scenario = scenarioRepository.findByName("RENT_ORDER_CREATED");
		Simulator simulator = new Simulator(scenario, eventBus, stateRepository);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Task completeScenarioTask = AwaitTask.builder()
				.name("Complete scenario!").duration(22L)
				.eventOnTaskComplete(eventRepository.findByName("RENT_STARTED"))
				.eventBus(eventBus)
				.simulator(simulator)
				.build();

		executorService.submit(completeScenarioTask);

		simulator.executeScenario();
	}
}
