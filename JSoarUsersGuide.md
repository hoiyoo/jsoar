#summary Using JSoar
#labels Featured

See also:
  * [Javadoc](http://darevay.com/jsoar/current/docs/jsoar-core/api/)
  * [Faq](Faq.md)
  * [JSoarDebugger](JSoarDebugger.md)
  * [JSoarScripting](JSoarScripting.md)
  * [JSoarPrinting](JSoarPrinting.md)
  * [JSoarEvents](JSoarEvents.md)
  * [JSoarInput](JSoarInput.md)
  * [JSoarOutput](JSoarOutput.md)
  * [JSoarRhsFunctions](JSoarRhsFunctions.md)
  * [JSoarCommands](JSoarCommands.md)
  * [JSoarSystemProperties](JSoarSystemProperties.md)
  * [JSoarLegilimens](JSoarLegilimens.md) - Remote, web-based debugging
  * [JSoarSemanticMemory](JSoarSemanticMemory.md)

_For information about using the JSoar debugger, see [JSoarDebugger](JSoarDebugger.md)_

# Introduction #

JSoar is essentially a library for constructing agent-based systems. It includes many conveniences, but you must still write both Soar code do define your agent's behavior as well as Java (or some JVM-based language) code to connect the agent to some environment.

# Essential JSoar Classes and Interfaces #
JSoar's API is broken up into a number of classes and interfaces. Although this makes testing and extension easier, it can lead to confusion and code that's more verbose than necessary. In the spirit of helper classes like [java.util.Collections](http://java.sun.com/javase/6/docs/api/java/util/Collections.html) and [java.util.Arrays](http://java.sun.com/javase/6/docs/api/java/util/Arrays.html), JSoar provides a number of classes which should almost always be favored over their raw interface counterparts:

  * [Wmes](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/memory/Wmes.java) - Methods for searching and filtering WMEs
  * [InputWmes](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/InputWmes.java) - Methods for adding and updating input WMEs
  * [Symbols](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/symbols/Symbols.java) - Methods for converting symbols to and from Java obejcts
  * [SoarCommands](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/commands/SoarCommands.java) - Methods for executing interpreter commands like "source"
  * [SoarEvents](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/events/SoarEvents.java) - Methods for common Soar event handling patterns.

# Project Setup #
Setting up a JSoar project depends a lot on your environment, build mechanism, and how you want JSoar to fit into your system. That said, a "vanilla" JSoar-based project can be created something like this:

  * Start up your favorite IDE
  * Create a new Java project
  * Add `jsoar-core-X.X.X.jar` and `jsoar-debugger-X.X.X.jar` to your project. It's typically best, to put the jars in a `lib` folder in your project and then add them rather than referring to an external copy of JSoar
  * Create a class with a `main` and create an agent as described below.

Additionally, the JSoar distribution comes with "source" jars which include all of JSoar's source code. This is generally useful for both debugging and figuring out what methods do when the JavaDocs leave something to be desired.

If you're not using an IDE, just add the jars above to your build and run classpath (with javac's -cp option) and compile away.

# Constructing a Raw Agent #
Constructing a new agent in jsoar is very straightforward:

```
import org.jsoar.kernel.Agent;

...
Agent agent = new Agent();
agent.setName("My Agent");
agent.getPrinter().pushWriter(new OutputStreamWriter(System.out));
agent.initialize();
agent.runForever(); // Call blocks until agent interrupts or halts
...
```

This constructs and initializes a raw Soar agent. The !Agent class includes base functions for running the agent (runFor() and runForever()) as well as interfaces for loading productions, modifying input, etc. The !Agent class makes no assumptions about how the agent will be run and is **not** thread-safe.

When you're done with the agent, clean it up:

```
agent.dispose();
```

# ThreadedAgent #
The ThreadedAgent class is a wrapper around a base !Agent instance which provides some higher-level run control. In particular it creates a new thread for the agent to run in and provides primitives for interacting with the running agent in a thread-safe way:

```
import org.jsoar.runtime.ThreadedAgent;

...
ThreadedAgent threaded = ThreadedAgent.create();
threaded.setName("My Agent");
threaded.getPrinter().pushWriter(new OutputStreamWriter(System.out));
threaded.runForever(); // Agent begins running in its own thread. Returns immediately.
...

```

Because the agent managed by `ThreadedAgent` is run in its own thread, `ThreadedAgent` provides an asynchronous interface for interacting with the running agent. This style ensures that the agent's data structures are only accessed from a single thread and also prevents deadlock. To execute code in the agent's thread, pass a `Callable` to the `execute()` method:

```
final Agent agent = threaded.getAgent();
threaded.execute(new Callable<Void>() {
    public Void call() throws Exception {
       System.out.println("WMEs: " + agent.getAllWmesInRete());
       return null;
    }
}, null);
```

note that the `call()` method is called in the agent thread. `execute()` also takes an optional second callable which is always called after the first command is executed. It is also called in the agent's thread. Note that `org.jsoar.runtime.SwingCompletionHandler` can be used in this context to marshal data back from the agent's thread to the Swing UI thread.

When you're done with the threaded agent, clean it up:
```
threaded.dispose();
```

# Opening the JSoar Debugger #
The JSoar debugger is not displayed by default in a custom project. To open it, you can use the `openDebugger()` (or `openDebuggerAndWait()`) method on your `ThreadedAgent` object (sorry, the debugger only works on `ThreadedAgent`). Alternatively, you can  start your agent running and have it call the `(debug)` RHS function.

# SoarCommandInterpreter #
JSoar uses an instance of `org.jsoar.util.commands.SoarCommandInterpreter` to process commands and load files. The interpreter is accessible through the `getInterpreter()` method of the `Agent` or `ThreadedAgent` class. A default interpreter (implemented by `org.jsoar.util.commands.DefaultInterpreter`) is created the first time this method is called.

JSoar includes an alternate, Tcl-based interpreter if you're so inclined. To enable it, make sure `jsoar-tcl.jar` is on your classpath, and set the `jsoar.agent.interpreter` system property to "tcl". See [JSoarSystemProperties](JSoarSystemProperties.md).

# Sourcing Soar Code #

The interpreter can source either normal files, or files accessible through a URL:

```
import org.jsoar.util.commands.SoarCommands;

...

SoarCommands.source(agent.getInterpreter(), "/path/to/file.soar");

... or ...

SoarCommands.source(agent.getInterpreter(), "http://darevay.com/jsoar/waterjugs.soar");
```

`SoarCommands.source()` is a helper method which automatically determines whether the string (or object) given is a File or URL. Also, keep in mind that Java resources are accessible through a "jar" URL so your source code can be embedded in a jar or just present on the classpath:

```

SoarCommands.source(agent.getInterpreter(), MyClass.getResource("/path/to/resource.soar"));

```

The source command keeps track of the "current working directory", even with URLs, so code on a web-server, or embedded in a jar can refer to additional files through relative paths.

**Note:** that when using `ThreadedAgent` all interpreter commands should be executed on the agent's thread since they normally manipulate agent state.

Currently, JSoar includes at least partial implementations of most of the built-in [Soar commands](http://winter.eecs.umich.edu/soarwiki/Soar_Command_Line_Interface).

# Agent Properties #
Arbitrary properties can be associated with an agent. See `org.jsoar.kernel.Agent.getProperties()` and `org.jsoar.kernel.SoarProperties` for examples of built-in properties.

_Property access is generally thread-safe, but may return structures that are not thread-safe._

Use the `properties` command to get a list of the current values of all agent properties.

# JSoar-specific RHS Functions #
See [JSoarRhsFunctions](JSoarRhsFunctions.md) for more info on JSoar-specific RHS functions and creating new RHS functions.

# Providing Input to the Agent #
See [JSoarInput](JSoarInput.md) for more info on generating agent input.

# Handling Agent Output #
See [JSoarOutput](JSoarOutput.md) for more info on handling agent output.

# System Properties #
See [JSoarSystemProperties](JSoarSystemProperties.md) for info on system properties used to control JSoar.