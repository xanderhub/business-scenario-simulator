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

<br />

![image](https://user-images.githubusercontent.com/33380175/64678921-42ead000-d483-11e9-9722-15700a86df63.png)
