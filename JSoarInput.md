See also [JSoarEvents](JSoarEvents.md), [JSoarOutput](JSoarOutput.md)
# Introduction #

There are several ways to provide input to a JSoar agent. They can all be used interchangeably and may be mixed as needed. Choosing which input method to use largely depends on your performance needs and how much code you want to type.

# The Input Phase and Threading #
It is important to understand the decision cycle of a Soar agent. In this case, we are most interested in the **input phase** of the decision cycle. Each time the agent enters the input phase an [InputEvent](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/events/InputEvent.java) is fired (see [JSoarEvents](JSoarEvents.md)). During this event, it is safe to modify the agent's input-link. Note that this event is always fired from the agent's thread so care should be taken to handle concurrency.

Some of the input methods described below will automatically register for InputEvent as well as handle modifying input from other threads.

# Core Input #
The core input API of JSoar is what all other input mechanisms are built upon. It is analogous (though easier to use) to the original input API of the C implementation of Soar (add\_input\_wme, remove\_input\_wme, etc). The key interface to study is [InputOutput](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/InputOutput.java). It includes methods for adding input WMEs. Here's an example which creates a structure rooted at the input link:

```
// Assume this is called from an InputEvent callback
InputOutput io = ...;
SymbolFactory syms = io.getSymbols();
Identifier addrId = syms.createIdentifier('a');
InputWme addr = io.addInputWme(io.getInputLink(), syms.createString("address"), addrId);
InputWme street = io.addInputWme(addrId, syms.createString("street"), syms.createString("123 Main"));
io.addInputWme(addrId, syms.createString("city"), syms.createString("Ann Arbor"));
```

This will create a structure like this on the input-link:

```
^state.io.input-link
   ^address
      ^street |123 Main|
      ^city   |Ann Arbor|
```

Later on, say you'd like to change the street. This can be accomplished with the `update` method of [InputWme](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/InputWme.java):

```
street.update(syms.createString("414 E. Lawrence"));
```

For optimal performance, it is common to cache symbols ahead of time and reuse them when creating WMEs.

# Input Helpers #
There are a number of helper methods for creating both symbols and input WMEs. These reside in [Symbols](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/symbols/Symbols.java) and [InputWmes](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/InputWmes.java). Using these, the example above would become:

```
// Assume this is called from an InputEvent callback
InputOutput io = ...;
SymbolFactory syms = io.getSymbols();
Identifier addrId = syms.createIdentifier('a');
InputWme addr = InputWmes.add(io, "address", addrId);
InputWme street = InputWmes.add(io, addrId, "street", "123 Main");
InputWmes.add(io, addrId, "city", "Ann Arbor");
```
Essentially, this helpers automatically convert Java Object (often auto-boxed primitives) to Soar Symbols.

# Input Builder #
[InputBuilder](http://code.google.com/p/jsoar/browse/trunk/jsoar-core/src/main/java/org/jsoar/kernel/io/InputBuilder.java) provides a [builder-like](http://en.wikipedia.org/wiki/Builder_pattern) interface to input working memory construction. With it, input structures can be built up with minimal code. Extending the example from above:

```
// Build initial structure, marking WMEs we want to remember
InputBuilder builder = InputBuilder.create(agent.io);
builder.push("person").
   add("name", "Dave").
   add("age", 56).
   push("address").
      add("street", "123 Main").markWme("street").
      add("city", "Ann Arbor").

...

// Retrieve the street WME and update it...      
InputWme street = builder.getWme("street");
street.update(syms.createString("414 E. Lawrence"));

```

# Quick Input #
The Quick Input framework is in the `org.jsoar.kernel.io.quick` package. The framework is broken into two parts, a [QMemory](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/quick/QMemory.java) instance which represents a tree of data, and a [SoarQMemoryAdapter](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/io/quick/SoarQMemoryAdapter.java) which automatically converts QMemory tree to input memory. It has the following features:

  * Thread-safe: the QMemory tree can be modified from any thread and will be correctly reflected in the agent's thread
  * Shareable: a single QMemory tree can be reflected simultaneously on the input-links of multiple agents.
  * String-driven hierarchy construction

Here is a quick example:

```
// Create a memory to act as the source. Any values we set in this object
// will appear on the agent's input-link 
QMemory memory = DefaultQMemory.create();
SoarQMemoryAdapter adapter = SoarQMemoryAdapter.attach(agent, memory);

// ... start running agent in this, or another thread ...

// Now make modifications to the memory
memory.setString("agent.info.name", "Patrick");
memory.setString("agent.info.role", "Baby");
```

This will create the following structure on the input-link:

```
^input-link
   ^agent
      ^info
         ^name Patrick
         ^role Baby
```

further changes to the `memory` object will be reflected. See the JavaDocs in the `org.jsoar.kernel.io.quick` package for more info.

# Other Input Components #
JSoar includes a few input components "out of the box" that can easily be added to an agent:

  * `org.jsoar.kernel.io.CycleCountInput` - Puts the current decision cycle on the input link.
  * `org.jsoar.kernel.io.TimeInput` - Puts the current time of day (system clock) on the input link.