# <Project Name> Project site

## Description

This project brings functionality to xtUML verify that will allow traveling through debugging history.  Actions are added for rewind, full rewind, fast-forward and full fast-forward.  Breakpoints are considered during travel.  Debugging states are maintained for all threads under a single launch even across inter-component communication.

## Requirements

R1.1 Execution state shall be traversable.   
R1.1.1 Each statement execution shall have history maintained.   
R1.1.2 While execution is paused all execution history shall allow backward traversal.   
R1.1.3 While execution is paused all execution history shall allow forward traversal up to the last statement executed.  
R1.1.4 The execution state shall exactly match the current exection history point when it was first executed.   

R1.2 Traversing   
R1.2.1 Backward   
R1.2.1.1 Rewind shall take execution history back a single history point  
R1.2.1.2 Full Rewind shall take execution history back to the execution point just before the first executed statement.  
R1.2.2 Forward  
R1.2.2.1 Fast-forward shall take execution history forward a single history point  
R1.2.2.2 Full Fast-forward shall take execution history forward to just after the last executed statement.  
R1.2.3 Breakpoints  
R1.2.3.1 All supported BridgePoint breakpoints shall be honored when traversing execution history.  
R1.2.3.2 Breakpoint enabled state shall be honored  
R1.2.4 Evaluation  
R1.2.4.1 During traversals through execution history evaluation of statements shall not reoccur.  
R1.2.4.2 Using the built-in continue and step commands shall truncate the forward execution history triggering re-evaluation.  

R1.3 Cross component communication   
R1.3.1 Execution history shall be maintained for all executions under a debug launch.   
R1.3.2 Traversing execution history shall handle intercomponent messaging.   
R1.3.2.1 Ordering shall match that which occurred during the evaluation execution.   
R1.3.2.2 Interface operation and signal invocations shall traverse to the thread owned by the message caller.  
  
R1.4 Enablement  
R1.4.1 All commands   
R1.4.1.1 All commands shall be enabled only if execution is paused.    
R1.4.2 Rewind commands    
R1.4.2.1 Rewind commands shall be enabled if there is at least one execution history entry.    
R1.4.3 Fast forward commands    
R1.4.3.1 Fast forward commands shall be enabled if there is at least one execution history entry.   
R1.4.2.2 Fast forward commands shall be enabled if the current point in execution history is not the last executed statement.    

## Timeframe  

This project has some of the work complete.   

Estimated time to delivery:  

March 21th of 2018  

## Demonstrations  
Video demonstrating time traveling debugger.  

<a id="Time Traveling Debugger"></a>[Time Traveling Debugger](https://youtu.be/7VRcZoqGDfM)  

## Pledging

This project uses the [Pledge Model](https://fmaysoftware.wordpress.com/pledging-model/) and has a target of ? USD.  

### Pledge to this project
Send an e-mail to [FMAY](mailto:travis.london@gmail.com) to pledge.  

Pledge status:  

![progress](http://progressed.io/bar/0 "progress")
 
