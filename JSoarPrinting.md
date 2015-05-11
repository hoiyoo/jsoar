See also:
  * [JSoarUsersGuide](JSoarUsersGuide.md)

# Introduction #
The agent trace/printing system in JSoar is significantly different (not
necessaryily better) from SML. In SML, anything the agent (or the kernel)
writes out triggers a print callback. In JSoar, you register a
[java.io.Writer](http://java.sun.com/javase/6/docs/api/java/io/Writer.html)
with the agent's [Printer](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/tracing/Printer.java).

# Printer #
The printer [Printer](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/tracing/Printer.java) is accessible
through the `Agent.getPrinter()` (or `ThreadedAgent.getPrinter()`) method. Though it's usually used internally by the agent to print information to the debugger trace, you can use it as well to print your own messages to the trace. It supported the usual printf formatting:

```
agent.getPrinter().startNewLine().spaces(10).print("Hello, %s", "World");
```

This will print a newline (if not already at the start of line), print 10 spaces, and then "Hello, World".

As mentioned above, writers are used to customize the output destination of the printer. A `Writer` can be registered with the `Printer` in two ways. The first
method is to register a **persistent** writer:

```
agent.getPrinter().addPersistentWriter(new OutputStreamWriter(System.out));
// Now all agent output (modulo trace settings) will be written
// to stdout.
```

If you wanted to log output to a file:

```
final Writer logger = new BufferedWriter(new FileWriter("soar.log"));
agent.getPrinter().addPersistentWriter(logger);
// Now all agent output (modulo trace settings) will be written
// to "soar.log"
```

A persistent writer receives **all** agent output regardless of any writers
that have been pushed on the writer stack (see below).

The second method is the writer stack. The top writer on the stack always
receives agent output. Pushing a new writer on the stack will cause output
to go to the new writer until it is popped:

```
// Push a temporary writer
final StringWriter temp = new StringWriter();
agent.getPrinter().pushWriter(temp);

// ... run agent or use the printer directlyr ...
agent.getPrinter().print("Hello");

// Pop the temporary writer
agent.getPrinter().popWriter();

// Do something with the captured output...
System.out.println("Agent said: " + temp);
```

**By default, no persistent or stack writers are installed on new agents
so you won't see any output**

**The printer is buffered so it may be necessary to call `agent.getPrinter().flush()` to force output to writers.**

# Trace #
The agent [Trace](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/tracing/Trace.java) is a facade around the
`Printer` which implements the trace output control settings associated
with the [watch](http://code.google.com/p/soar/wiki/CommandLineInterface#watch) command. The trace defines a number of output categories which can be controller with the `Trace.setEnabled()` method.

`Trace` has the same `print` methods as `Printer`, exception that they take an additional `Category` parameter used for filtering:

```
agent.getTrace().print(Category.VERBOSE, "HI!");
```

In this case, "HI!" will only be printed when the agent is in verbose mode, e.g. `verbose --on`.