#summary JSoar release change log
#labels Featured

### 0.12.0 (released 6/16/2011) ###

  * Upgrade CSoar support in SoarUnit to 9.3.1
  * Various SoarUnit fixes
  * Ported various bug fixes from CSoar

### 0.11.0 (released 9/13/2010) ###
  * Introduced [JSoarScripting](JSoarScripting.md), a framework for embedding I/O scripts directly in Soar source files.
  * Introduced [SoarUnit](SoarUnit.md), a unit testing framework for Soar. It works with both JSoar and CSoar 9.3.0.
  * Converted integers and identifier counts to 64-bit values
  * Implemented **pwatch** command
  * Implemented **pbreak** command. Dynamically set match-time interrupts on productions. Same as `:interrupt`, but there's no need to modify the code.
  * Implemented support for `:interrupt` flag on productions.
  * Various memory usage and performance improvements
  * Better reporting of files and line numbers for rules and commands. Commands have access to this info now.
  * Added a goal stack display to the JSoar debugger
  * In debugger, implemented scroll lock in trace.
  * Add option to limit trace output in debugger.
  * In debugger, pasting to trace window executes commands from clipboard.
  * In debugger, double-click identifiers in debugger trace window to print/drill-down
  * In debugger, replace [FlexDock](https://flexdock.dev.java.net/) with [DockingFrames](http://dock.javaforge.com/)
  * General debugger cleanup
  * Support for partial command matches
  * Switched logging from commons-logging to slf4j.

### 0.10.0 (released 7/15/2010) ###
  * Ported CSoar 9.3.0's (actually slightly newer, but functionally equivalent) implementation of semantic memory. See [JSoarSemanticMemory](JSoarSemanticMemory.md).
  * Updated JSoar's reinforcement learning implementation to match that of CSoar 9.3.0
  * Implemented `org.jsoar.kernel.rete.SimpleMatcher` which allows the Soar rete to be used independently of the rest of Soar. We use this to simplify testing of Soar features like GDS.
  * Nearly complete implementation of the print command. Only `--varprint` is missing.
  * Numerous new functional/spec tests for kernel (impasses, initial state, gds, etc)
  * More complete implementation of run command
  * More complete implementation of **watch** command
  * Implemented -a, -v, and -d flags for source command (source details)
  * Implemented **set-stop-phase** command
  * Implemented **gds-print** command
  * Implemented **explain-backtraces** command
  * Implemented **version** command
  * Implemented **timers** command
  * Implemented **debugger** command. Opens new debugger.
  * "format" RHS function equivalent to sprintf in C
  * "list" RHS function for building linked lists
  * Implemented output-link change tracking (OutputEvent.getChanges())
  * Generalized WmeFactory interface used by both RHS functions and input producers
  * Performance and memory usage improvements by optimizing MatchSetChange and Slot linked lists.
  * Improved, color coded partial match view in debugger.
  * Port of [soar2soar](http://code.google.com/p/soar/source/browse/tags/Soar-Suite-9.3.0/Soar2Soar) to JSoar.
  * Finer-grained control of GDS WME removal messages
  * Removed a number of Soar 7 remnants (preference phase, wm phase, etc)
  * Upgraded/switched from now-defunct Google Collections Library to [Google Guava](http://code.google.com/p/guava-libraries/) libraries

### 0.9.1 (released 5/17/2010) ###
  * Code moved to Mercurial (https://jsoar.googlecode.com/hg)
  * Improved memory usage by eliminating many cases of long-lived, small objects, especially in linked-list management
  * To reduce memory usage, moved Tcl interface into separate, optional library, jsoar-tcl.jar. Set **jsoar.agent.interpreter** system property to **tcl** to re-enable Tcl interpreter.
  * String symbols are no longer needlessly encased in pipes when they contain capital letters. Yay!
  * Ported various fixes from CSoar (thanks Jon Voigt).
  * General hardening from actual use.

### 0.9.0 (released 10/30/2009) ###
  * Added [JSoarLegilimens](JSoarLegilimens.md)
  * Fixed waitsnc with no args
  * Fixed some threading issues where `wait` RHS function would spuriously wake up even though no new input was available ([r542](https://code.google.com/p/jsoar/source/detail?r=542))
  * Fixed attribute handling in preferences command ([r539](https://code.google.com/p/jsoar/source/detail?r=539))
  * Improved handling of unhandled exceptions in agent thread
  * Port of various CSoar fixes ([r538](https://code.google.com/p/jsoar/source/detail?r=538), [r537](https://code.google.com/p/jsoar/source/detail?r=537))
  * Major improvements to trace formatting ([r572](https://code.google.com/p/jsoar/source/detail?r=572))

### 0.8.2 (released 7/23/2009) ###

  * Implemented production-find command. Does not print bindings yet.
  * Helper functions for sticking an XML file on input-link from any thread
  * Fix for --depth handling in print command

### 0.8.1 (released 7/18/2009) ###

  * Made `org.jsoar.kernel.symbols.Symbols.NEW_ID` play nicer with scripting languages
  * Store file info with productions
  * implement edit-production command
  * implement `--filename` option of `print` command
  * Incremental, regex search of trace window
  * Added `openDebuggerAndWait()` method for more predictable startup use case.

### 0.8.0 (released 7/10/2009) ###

  * Fixed printing of math RHS functions
  * Added `min` and `max` RHS functions
  * Implemented `random-int`, and `random-float` RHS functions as discussed in UofM Bugzilla [issue 800](https://code.google.com/p/jsoar/issues/detail?id=800)
  * Added `exec` RHS function for easier transition from SML-based agents
  * A number of cleanups to the API driven by some serious dog-fooding
  * Various Ruby API improvements
  * Added input component that creates a queue of XML messages on the agents input-link
  * Strong start on implementation of SML API in JSoar. Currently can run Towers of Hanoi and Missionaries and Cannibals demos without recompiling just by swapping jsoar-sml.jar in for sml.jar

### 0.7.2 (released June 27, 2009) ###

  * Implemented "memories" command
  * Better error message when JavaScript engine is missing (Mac OS)
  * Use 64 bits for counters (decisions, elaborations, etc)
  * Fixed major memory leak which caused justifications to never be garbage collected.
  * Made ThreadedAgent Adaptable.
  * Print "stars" while loading produdtions
  * Print errors when loading files from debugger's command-line
  * Remember file chooser directory across debugger sessions

### 0.7.1 (released June 24, 2009) ###

  * Fix major memory leak which caused instantiations to never be garbage collected. Memory usage is a little less crazy now.
  * Implemented "internal-symbols" command
  * Added "Run Garbage Collector" command to debugger
  * Implemented missing "mod" RHS function

### 0.7.0 (released June 22, 2009) ###

  * Ruby work including sample twitter interface for Soar agent's
  * Reworked the input API a bit to make it a little friendlier. InputWme.remove() is not thread-safe.
  * Eliminated duplicate RHS values
  * Cache common RHS values
  * Support for reading and writing (mostly) arbitrary XML. Old XML conversions moved to SoarTechWmeToXml, etc.
  * Fix lexer crash on unicode characters
  * print --rl
  * print --exact (thanks Bob!)
  * Made QMemory compatible with (wait) function
  * Implemented SoarBeans and added output manager based on them
  * Cleaned up remaining public members of Agent class
  * Made command implementations Tcl neutral
  * Replaced int transitive closure markers with real objects (Marker)
  * Renamed InputCycleEvent to just InputEvent for consistency
  * Full support for sourcing from URLs (includes pushd, popd, etc)
  * Remember preferences in debugger
  * Major speedup in debugger trace window
  * Added Time input component
  * Added WME Search view to debugger
  * Jon Voigt ported several bug fixes over from CSoar.
  * preferences command
  * Trace context menu

### 0.6.0 (released May 2, 2009) ###

  * Revamped project structure
  * Extensive refactoring on public interfaces
  * Removed Soar 7 compatibility mode
  * Removed support for o-support modes 0 through 3
  * Clean up and refinement of agent threading model
  * Implemented "debug" RHS function. Opens a debugger.
  * Improved interfaces for embedding debugger
  * Debugger UI improvements
  * Implemented "wait" RHS function. Suspends agent thread until new input is available
  * Implemented excise and firing-counts commands
  * Implemented "to-xml" and "from-xml" RHS functions. Convert simple XML to working memory.
  * Implemented "get-url" RHS function. Requests the contents of a URL.
  * Implemented run, stop and init-soar commands
  * Added basic URL support to source command

### 0.5.0 (released Dec. 12, 2008) ###

  * Very basic implementation of print command. Options must come first, i.e. "print -d 5 s1" rather than "print s1 -d 5"
  * Implemented [JavaRhsFunction](JavaRhsFunction.md)
  * Implemented **tcl** RHS function
  * Implemented **deep-copy** RHS function
  * Added APIs to allows RHS functions to create working memory structures, not just return a value
  * Implemented **int** and **float** RHS functions
  * Implemented simple production editor to debugger. Double-click production to edit

### 0.0.1 (released Dec. 9, 2008) ###
Initial public release