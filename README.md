# business-scenario-simulator  
 {*under construction*} <br />
 App for simulating business scenarios based on FSM (Final State Machine) principal

## Description
This project is a sort of business process simulator. It aims to help in building and running different business <br /> 
scenarios (flows). Unlike [BPMN](https://en.m.wikipedia.org/wiki/Business_Process_Model_and_Notation) (Business Process Modeling Notation) it based on [FSM](https://en.wikipedia.org/wiki/Finite-state_machine)-like (Final State Machine) flowcharting i.e. it has __States__, __Transitions__ and __Events__ for modeling business scenarios. <br />
<br />
Simple example of state machine:<br />
![image](https://user-images.githubusercontent.com/33380175/64645410-86125800-d41d-11e9-9b1a-683536c313fb.png)
<br />

In this simulator each entity has the same purpose as in a state machine: <br />
__State__ - i.e. some business case <br />
__Transition__ - defines transition from one case (state) to another <br />
__Event__ - signals about changes during simulation process, triggers transitions between states <br />

In order to increase flexability and computational power __Task__ entity has been added. __Tasks__ can execute <br />
different business tasks like quering database, sending email / sms or just wait for some specified period of time. <br />
Each __State__ can have multiple __Tasks__ to be executed. __Task__ can also emit predefined __Events__ and trigger changes.<br />
The simulator is event-driven application. It accepts external and internal __Events__ and decides about changes based on
current __State__.

## Scenario example
There is a demo scenario in this project defined in `SimulatorBootstrap` class. It describes a bussiness logic for specific case 
that can occur in car sharing service. The following scenario designed to prevent false, accidental or fake orders. Suppose, some customer makes a car rent order (i.e - creates rent order event). At this point our scenario simulator is in a *starting* state - "Order received". At this state simulator waits (`AwaitTask` is triggered) for customer to start using a car - i.e. for specific *event* called "Rent started" or "Rent canceled". If such event occurs (in any state) scenario will be completed and simulator will finish at "Rent started / canceled" state. In case there was no such event for specified period of time `AwaitTask` will complete and trigger "Rent didn't start" event. The Simulator will process this event and change it's state according to the scenario - "Send push notification to customer". Same logic here - waiting for rent start / cancel event and changing the state after waiting. The simulator will then notify customer by SMS and after that the case will be forwarded to call center where operator calls customer and decides to cancel or continue rent order.
<br />

![image](https://user-images.githubusercontent.com/33380175/64677847-0fa74180-d481-11e9-97c6-03d353c5369d.png)

<br />

## Project design

### Database
The following is a database design used to store all the entities mentioned above (__State__, __Task__ etc.).<br />
In this project H2 database is used in-memory mode for demo purposes.
* __Scenario__ entity is a "biggest" one. It's related to multiple __State__ entities and describes the whole process.
* __State__ is a child entity of __Scenario__. It describes some business case and can execute multiple tasks. It also can have multiple transitions attached to it. State can be one of three types: *InitState*, *FinalState* and *IntermediateState*.
* __Task__ is a child entitiy of __State__. Can be in multiple types: *AwaitTask* - task that waits (timer), *ConditionalTask* - fires different events based on provided condition (not implemented yet), *ScriptTask* - executes scripts (not implemented yet), and many other tasks to be implemented...
* __Transition__ entity that describes transition between states. Each transition has *source* and *target* state and __Event__ which "triggers" it. Transition can be "triggered" by only one __Event__ i.e. it has one-to-one relationship.
* __Event__ entity that drives the simulation process (by triggering transitions) and being fired by __Task__ entities. Event can be internal - produced by some task, or external - produced by operator or some other simulator (if connected to the same event bus) 
<br />

![image](https://user-images.githubusercontent.com/33380175/64678921-42ead000-d483-11e9-9722-15700a86df63.png)


### Application - Scenario builder
The project is built with Java 8 and Spring Boot framework. It also utilizes [spring reactor](https://projectreactor.io/) for dispatching events inside and outside simulation process. The app is using H2 database (in-memory mode) for storing mentioned entities. On application startup mentioned scenario is being built by `buildScenario()` method in `SimulatorBootstrap` class:

First it builds the Scenario object
```
Scenario scenario = Scenario.builder()
                .name("RENT_ORDER_CREATED")
                .description("This scenario checks for car rental commitment after receiving rental order from customer")
                .build()
                .saveTo(scenarioRepository);

```

Then State objects are being declared and built:
```
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
```

Then Transitions are defined between the states:
```
Transition start_to_finish = Transition.builder()
                .sourceState(waiting_for_rental_start).targetState(rent_started)
                .onEvent(Event.builder().name("RENT_STARTED").eventType(EventType.COMPLETE_SCENARIO).build().saveTo(eventRepository))
                .build()
                .saveTo(transitionRepository);
```
Note, here Transition object declared with its "triggering" Event object. See `onEvent()` setter  <br />
Then the script is adding tasks (`AwaitTask`) for each State object:
```
waiting_for_rental_start.addTask(AwaitTask.builder().state(waiting_for_rental_start)
                .name("Waiting for user to start the rent").duration(5L)
                .eventOnTaskComplete(start_to_push_user.getOnEvent())
                .build().saveTo(taskRepository));
```
Note, here Task is defined to fire Event object on completion in `eventOnTaskComplete()` setter.
Duration of each `AwaitTask` object is set to 5 seconds for demo purposes. <br />
This script actually simulates some UI's output where end-user defines the business flow diagram  <br />

### Application - Simulator
In `run()` method of `BusinessScenarioSimulatorApplication` the Simulator object is being created and started:
```
  Scenario scenario = scenarioRepository.findByName("RENT_ORDER_CREATED");
		Simulator simulator = new Simulator(scenario, eventBus, stateRepository);
  
  simulator.executeScenario();
```
Simulator needs a Scenario to execute, EventBus to get events from inside and outside and states repository to navigate between the states of business process. Once created, Simulator registers to the EventBus as a consumer and waits for incoming events.

### Application - State
Each State object can run multiple tasks, but in sequential order. It has an ExecutorService that allows to run only one thread at a time, so tasks will be executed one by one.
State has `runOnSimulator()` method that Simulator calls to run the state:
```
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
```
When executing the method State registers all its tasks to Simulator's event bus, so Tasks could send events to it. 
When state is about to change as a result of some external event all its tasks need to be closed and no events should be fired.
During transition between states the simulator shuts down all current tasks by calling `currentState.cancelAllTasks()` and switches to the next state `currentState = transition.getTargetState()`

### Application - Task
Currently it is only await task type supported (`AwaitTask`). This type of tasks can fire event before timer starts and after it completes. The following method sends event on task complete: 
```
    private void eventOnComplete() {
        if (eventOnTaskComplete != null && !isStopped) {
            eventBus.notify(object(simulator).getObject(), reactor.bus.Event.wrap(eventOnTaskComplete));
            LOG.debug("eventOnTaskComplete fired - {}", eventOnTaskComplete);
        }
    }
```
Timer task itself defined simply as following:
```
@Override
    protected void execute() {
        if (duration != null)
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                LOG.debug("Task - {} - shut down", getName());
                setIsStopped(true);
            }
    }
```
`isStoped` flag used when simulator shuts down all current tasks and switches to another state.
