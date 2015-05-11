# Introduction #

This page describes the JSoar-specific RHS functions included with JSoar. It also describes the process of defining and installing your own RHS functions.

RHS functions in JSoar are more powerful than their SML/CSoar counterparts for several reasons:

  * They can take individual, non-string arguments. In SML, all arguments are concatenated together into a single string, leaving the implementer to parse them back out.
  * They can take **identifiers** as arguments. This means a RHS function can traverse working memory to do its thing.
  * They can return **arbitrary structures**, not just strings. All the WMEs created by the function are given the same support as the rule. This could be used to create RHS "macros" for constructing frequently used structures like linked lists, etc.
  * They are registered on a per-agent basis rather than for all agents. This makes it easier to have a system of heterogeneous agents.

A listing of all currently installed RHS functions can be seen with the `rhs-functions` command.

# Built-in RHS Functions #

There are a number of RHS functions include with JSoar that aren't available in the standard implementation of Soar:

  * [java](JavaRhsFunction.md) - Call Java code from an agent
  * `(debug)` - Open a debugger for the agent
  * `(wait)` - Cause the agent's thread to sleep until new input is available. Takes an optional millisecond timeout parameter, e.g. `(wait 5000)` would wait 5 seconds and then wake up regardless of whether there is new input available.
  * `(get-url <url>)` - Retrieve the contents of the given URL (a string) and return it as a string. Useful in conjunction with `(from-xml)`.
  * `(to-xml <id>)` - Converts the working memory under the given id into XML and returns it as a string
  * `(from-xml <xml>)` - Converts the given XML string to working memory and returns an identifier
  * `(split <string> <regex>)` - Splits a string with the given regular expression. Returns a linked-list of strings. Behaves the same as [java.lang.String.split](http://java.sun.com/javase/6/docs/api/java/lang/String.html#split(java.lang.String).
  * All of the methods in [java.lang.Math](http://java.sun.com/javase/6/docs/api/java/lang/Math.html) are available as RHS functions.
  * `(e)` - Returns the constant `e`.
  * `(pi)` - Returns the value of `pi`.
  * `(max <a> ...)` - Returns the max of 1 or more numbers. Preserves the type (int/double) of the maximum that was found.
  * `(min <a> ...)` - Returns the min of 1 or more numbers. Preserves the type (int/double) of the minimum that was found.
  * `(list 1 2 3 4)` - Builds a linked list of its arguments
  * `(format <format-string> <args...>)` - Equivalent to sprintf in C or String.format in Java.

Here's an example (possibly useless) that reads an RSS feed into working memory using the `get-url` and `from-xml` functions:
```
sp {get*rss*feed
  (state <s> ^superstate nil)
-->
  (<s> ^rss (from-xml (get-url |http://www.nytimes.com/services/xml/rss/nyt/International.xml|)))
}
```

# Creating Your Own RHS Functions #
_Note that there are several useful helper routines in the [org.jsoar.kernel.rhs.functions.RhsFunctions](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/RhsFunctions.java) class._

Creating a new RHS function is fairly simple. RHS functions implement the [RhsFunctionHandler](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/RhsFunctionHandler.java) interface. Two base implementations of this interface are included with JSoar:

  * [AbstractRhsFunctionHandler](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/AbstractRhsFunctionHandler.java) - Base handler for RHS functions that return a value and are not called in a "standalone" manner.
  * [StandaloneRhsFunctionHandler](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/StandaloneRhsFunctionHandler.java) - Base handler for RHS functions that are meant to be called in a "standalone" manner, i.e. they're return value is not used in constructing WMEs. e.g. `halt`, `interrupt`, etc.

To create you new RHS function, subclass one of these classes, passing the function's name, minimum number of acceptable arguments, and maximum number of acceptable arguments to the superclass constructor. If `min` and `max` are omitted it is assumed the function can take any number of arguments.

Next, implement the `execute` method. This methods takes two arguments, a [RhsFunctionContext](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/RhsFunctionContext.java) and a list of arguments as `Symbol`s:

```
public Symbol execute(RhsFunctionContext context, List<Symbol> arguments) 
```

It must return a new symbol that is the result of the function (standalone functions may return `null`). The [RhsFunctionContext](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions/RhsFunctionContext.java) object give access to:

  * the `SymbolFactory` which may be used to create new symbols
  * the production that is currently firing, causing the function to execute
  * a method that will cause new WMEs to be created after the function returns. This is how a RHS function may return complex structures.

Once your function is implmemented, it must be registered with the agent using the `RhsFunctionManager`:

```
agent.getRhsFunctions().registerHandler(new MyRhsFunction());
```

The best way to learn how to implement a RHS function is to look at the source for the existing functions, which can be found in the [org.jsoar.kernel.rhs.functions](http://code.google.com/p/jsoar/source/browse/#hg/jsoar-core/src/main/java/org/jsoar/kernel/rhs/functions) package.