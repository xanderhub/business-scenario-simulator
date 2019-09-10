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

In order to increase flexability and computational power to this simulator __Task__ entity has been added. __Tasks__ can execute <br />
diffrent business tasks like quering database, sending email / sms or just wait for some specified period of time. <br />
Each __State__ can have multiple __Tasks__ to be executed. __Task__ can also emit predefined __Events__ and trigger changes.<br />
The simulator is event-driven application. It consumes __Events__ and decides about changes based on current __State__.


