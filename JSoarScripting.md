#summary JSoar Scripting Support

_See also [this blog post](http://blog.darevay.com/2010/08/scripting-jsoar/)_

# Introduction #

JSoar has built-in support for implementing simple environments by embedding JavaScript, Python, or Ruby code directly in Soar source files. It accomplishes this with help from the [JSR-223 Java Scripting API](https://scripting.dev.java.net/). Any language that has JSR-223 support can be used.

JSoar scripting can be used for many things:

  * Implement simple agent environments
  * Quickly implement new RHS functions
  * Generating I/O for SoarUnit tests!
  * Implement simple custom user interfaces for agents

# The Script Command #
Scripting is supported by the **script** command. No engine is initialized until the first call to the script command. The general format of the command is:

```
script [general options] <engine-name> [engine options] ... code ...
```

The first non-option argument is the engine name, typically the name of the language, "javascript", "python", "ruby". This is followed by engine-specific options (there are none at the moment). Finally the remaining arguments are joined together with spaces and evaluated as arbitrary code in the script engine. Enclose arguments in braces to include newlines.

For example

```
script javascript {

    soar.print("hello, Soar");

}
```

Note that the script command can be run interactively in the JSoar debugger.This allows inspection of the environment at runtime. To make this more convenient, default aliases are set up for JavaScript, Python, and Ruby:

```
> js soar.print("Hello from JavaScript");

> py soar.trace("Hello from Python");

> rb soar.print("Hello from Ruby");
```

_Note: In Python 2.X, **print** is a keyword so the method has been named **trace** instead._

The following general options are supported:

  * `--dispose` or `-d`: Disposes the engine, cleaning up any resources and detaching it from the agent.
  * `--reset` or `-r`: Reinitializes the engine. This is equivalent to disposing and recreating the scripting engine.

The `--reset` option is useful for files you'll be sourcing multiple times in the same JSoar session. So a common approach is to reset the engine in your first sourced file:

```
script --reset javascript {
    ... your code here ...
}
```

# Setup #
This section describes setup necessary for various scripting engines. The short answer is that any JSR-223 compliant engine can be added to !JSoar's classpath and it will be automatically available.

## JavaScript ##

JavaScript support is included with Java 1.6 by default. Thus, no additional setup is necessary to use it.

_Note that on Mac OS X, the JavaScript engine might not be included by default. [This page](http://jmesnil.net/weblog/2008/05/14/how-to-include-javascript-engine-in-apples-java-6-vm) describes the process of installing it on OS X._

## Python ##

[Jython](http://jython.org) provides a Java implementation of Python. To use it, download and install Jython and then add jython.jar to your classpath. The first time the engine is used, there may be a lag as Jython caches information about your Java environment.

## Ruby ##

[JRuby](http://jruby.org) provides a Java implementation of Ruby. JSoar scripting has been tested with JRuby 1.5.2. To use it, download and install JRuby and then add jruby.jar to your classpath.

# Examples #
There are example implementations of a waterjugs environment in JavaScript, Python and Ruby. There's a common Soar file with the agent's rules and then separate files for each scripting engine.

In each example, the input-link looks like:

```
^input-link
  ^jug
    ^name a
    ^capacity 5
    ^contents 0
  ^jug
    ^name b
    ^capacity 3
    ^contents 0
```

and the output commands are:

```
# Fill a jug
^output-link
  ^fill
    ^jug <name-of-jug>

# Empty a jug
^output-link
  ^empty
    ^jug <name-of-jug>

# Pour from one jug to another
^output-link
  ^pour
    ^from <name-of-jug>
    ^to   <name-of-jug>
```

## JavaScript ##
[Here](http://code.google.com/p/jsoar/source/browse/jsoar-demos/demos/scripting/waterjugs-js.soar) is the JavaScript example.

## Python ##
[Here](http://code.google.com/p/jsoar/source/browse/jsoar-demos/demos/scripting/waterjugs-py.soar) is the Python example.

## Ruby ##
[Here](http://code.google.com/p/jsoar/source/browse/jsoar-demos/demos/scripting/waterjugs-rb.soar) is the Ruby example.

**Don't forget that you can source these URLs directly in the JSoar debugger!**

# Scripting API #

# RHS Functions #
When an scripting engine is initialized, a RHS function with the same name as the engine is automatically installed in the agent. The function takes the arguments, joins them together into a single string (i.e. joined with an empty string) and then evaluates the expression in the engine. For example:

```
sp {test
  (state <s> ^superstate nil)
-->
  (javascript |soar.print("The top state is | <s> |")|)
}
```

Note that proper escaping is necessary and all issues with evaluating arbitrary text in a scripting language applies. Be careful. Generally, it's easier to install a custom RHS function using the scripting API described above, i.e. **soar.rhsFunction()**.