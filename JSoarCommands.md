# Introduction #

JSoar adds a few new commands on top of those inherited from the C implementation of Soar. Additionally, it is pretty easy to install new commands in JSoar. This page describes both.

# JSoar-specific Commands #

  * `properties` - list current values of agent properties
  * `rhs-functions` - list all installed RHS functions along with min/max number of arguments
  * `qmemory` - A command-line interface to `QMemory`. See [JSoarOutput](JSoarOutput.md) for more info.

# Creating New Commands #
Implement the [SoarCommand](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/util/commands/SoarCommand.java) interface and register it like this:

```
    agent.getInterpreter().addCommand("name-of-command", commandObject);
```

Once that is done, your command is usable both from Soar source code and the debugger.