# General #
## How do I get the agent output to appear? ##
You've constructed an agent, but no output appears in the console when you run it. See [JSoarPrinting](JSoarPrinting.md).

## Does JSoar run on Mac? ##

[Yes.](RunningJSoarOnMac.md)

## What about SML? ##
SML is the integration API for the original C implementation of Soar. It is a C++ interface with SWIG-generated bindings for Java, Python, C#, and Tcl. It's a fine API although because it relies on SWIG to support other languages, it's difficult to really add a lot of helpers since this would add to the burden of maintaining all the SWIG bindings.

Anyway, a lot of people have existing Soar systems built using the Java SML bindings (`sml.jar`). To facilitate moving over to JSoar, as of [version 0.8.0](ChangeLog.md), a bare bones implementation of the SML API is provided with JSoar. This takes the classes and interfaces from `sml.jar` and implements them in terms of the JSoar API. In fact, the existing Java Towers of Hanoi and Missionaries and Cannibals demos can be run just by replacing `sml.jar` with `jsoar-sml.jar`. No compilation is necessary.

Now, in most cases it won't be that easy. The SML implementation's primary focus is to ease the transition to JSoar for all the really tedious parts of Soar integration. Namely, I/O and RHS functions. To this end, it is possible to get an `sml.Agent` interface which is backed by a JSoar agent and then use your existing I/O code. The run control logic will probably have to be moved over to JSoar. This process is still not that well documented. Take a look at the code and especially the tests in the [jsoar-sml](http://jsoar.googlecode.com/svn/trunk/jsoar-sml) module.