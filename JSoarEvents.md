# Event Handling #

A lot of interaction with the agent happens through callbacks. JSoar includes a centralized event system with which listeners can be registered and events received. The two important interfaces are [SoarEventManager](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/events/SoarEventManager.java) and [SoarEventListener](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/events/SoarEventListener.java). Client code implements the `SoarEventListener` interface and then registers with the `SoarEventManager`, which is accessible through `agent.getEvents()`. All events are **typed**, i.e. they are represented by Java classes directly rather than implicitly through listener method names or something. Here's an example of registering for an `InputEvent` using an anonymous class:

```
agent.getEvents().addListener(InputEvent.class, new SoarEventListener() {

    @Override
    public void onEvent(SoarEvent event)
    {
         InputEvent ie = (InputEvent) event;
         // do something, such as adding/removing input WMEs ...
    }});
```

# Threading #
All agent-related events are fired from the agent's thread so be careful! Be careful which locks you hold and how long your event handler takes to complete. While the event is being handled the agent will not run.

# Custom Events #
Custom events can be defined by implementing the [SoarEvent](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/events/SoarEvent.java) interface. They can be fired with the `SoarEventManager.fireEvent()` method.