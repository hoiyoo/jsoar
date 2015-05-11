# Overview #
Currently, Waterjugs, Towers of Hanoi, Eight Puzzle, Blocks World, and Arithmetic examples from the 9.0.0 distribution all succeed in JSoar. That is to say, the full kernel of Soar 9.0.0 is implemented and functioning well. I'm working on a port of the SML layer to allow jsoar to be a drop-in replacement for csoar in any existing Java-based Soar projects.

# Details #

Here's the status of the porting of various source files in the kernel proper.

  * Symbols - done
  * Lexer - done
  * Parser - done
  * Reorderer - done
  * Rete - functional
    * No code for saving/loading binary rete
  * wmem.cpp - done (org.jsoar.kernel.memory.WorkingMemory, etc)
  * tempmem.cpp - done (org.jsoar.kernel.memory.TemporaryMemory, etc)
  * prefmem.cpp - done (org.jsoar.kernel.memory.PreferenceMemory, etc)
  * osupport.cpp - done (org.jsoar.kernel.memory.OSupport)
  * recmem.cpp - done (org.jsoar.kernel.memory.RecognitionMemory)
  * decide.cpp - done (org.jsoar.kernel.!Decider)
  * io.cpp - done (org.jsoar.kernel.io.InputOutput)
  * decision\_manipulation.cpp - done (org.jsoar.kernel.DecisionManipulation)
  * consistency.cpp - done (org.jsoar.kernel.!Consistency)
  * exploration.cpp - done (org.jsoar.kernel.!Exploration, etc)
  * RHS Functions
    * The basic framework is in place as well as a couple of basic functions (write, etc)
  * trace.cpp - done (org.jsoar.kernel.tracing.TraceFormats)
  * backtrace.cpp - done (org.jsoar.kernel.learning.!Backtracer)
  * callback.cpp - done, but differently (org.jsoar.kernel.events)
  * chunk.cpp - done (org.jsoar.kernel.learning.!Chunker)
  * init\_soar.cpp - Mostly done (decision cycle and initialization)
  * print.cpp - started
  * production.cpp - Mostly done (Production.java, etc). Porting additional code as needed.
  * reinforcement\_learning.cpp - done but **untested**
  * explain.cpp - done

# Ongoing Synchronization #

[Soar 9](http://soar.googlecode.com) is a moving target, this table is to help with mapping jSoar revisions to Soar 9 revisions. This information must be assumed to be incomplete. More detailed comments are provided in the commit messages.

| **jSoar revisions** | **Soar 9 revisions** | **Notes** |
|:--------------------|:---------------------|:----------|
| 302 | 10477 | force-learn |
| 334 | 10564, 10560, 10557 | Bugzilla [bug 1144](https://code.google.com/p/jsoar/issues/detail?id=144) |
| 336 | 10562 | state removal messages |
| 337 | 10565 | Bugzilla bugs 1144, 1011: marks higher-states changed when GDS removes state so impasses regenerated |
| 338 | 10567, 10568 | Bugzilla [bug 1145](https://code.google.com/p/jsoar/issues/detail?id=145) fix, but **noticed rhs function actions dont-learn and force-learn unimplemented** which are required before tests from csoar can be ported |
| 352 | 10608 | Bugzilla [Bug 517](https://code.google.com/p/jsoar/issues/detail?id=17) fix ported to jSoar |
| 538 | 11036 | Corrected select command |
| 537 | 10938 | Added clear of id.unknown\_level on garbage collected ids. |
| 584 | 10571, 10572, 10642, 10643  | Bugzilla [bug 234](https://code.google.com/p/jsoar/issues/detail?id=34), implemented new preference semantics for better/worse filter |
| 587 | 10935 | Flattened out two sets of cyclical recursive functions, used in kernel garbage collection, that would cause stack overflow with long, deep working memories (such as a "linked list" of identifiers) |
| 589 | 11001 | Added missing clear of chunk-free problem spaces when goal is removed |