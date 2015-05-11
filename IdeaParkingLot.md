# Introduction #

This page is a general parking lot for random ideas of how jsoar can be made cool. Ideas should be given a brief description here and then fleshed out in greater detail on their own page.

# The Ideas #

### SoarBeans ###
Annotated Java objects (like [JAXB](https://jaxb.dev.java.net/)) are automatically converted to input wmes and vice versa. Difficult issues off the top of my head:

  * Handling change notification, i.e. when does a field of a bean need to be updated?
  * Multi-threading. If agent is running in separate thread from whatever's updating the bean, how do we handle that? We want this to be **easy**.

[Drools](http://en.wikipedia.org/wiki/Drools) already does this in some ways. Maybe investigate their approach?

### Agent Run Control Design ###
Design a run control framework that supports the following:

  * Run a single agent manually in a client thread
  * Run a single agent in its own thread
    * Handle one-off debugger commands
    * Drop into synchronous debugger loop on interrupt
  * Cases above, but for multi-agents run serially
  * Clean API that handles difficult details of everything above. Like DocumentThread in Java Soar Debugger, but less ad hoc.
  * Support for "sleeping" agents. That is, the thread of an I/O-bound agent should actually sleep while waiting for input. Don't hog the CPU doing a wait operator.
    * Not sure about how useful this will be for most complex agents. A real-time agent probably has a clock, position, or other things on the input-link that change every cycle anyway (hence, no sleeping). For those agents that can make use of this kind of thing, it should probably be part of waitsnc, as opposed to just detecting state no-changes or actual wait operators (so people can use or not at their discretion).

### Java RHS Function Integration (JavaRhsFunction) ###
**Done. Implemented 12/13/2008**
Allow Java methods to be called reflectively from RHS functions:

  * Add new symbol type to hold Java object references
  * Design syntax for calling methods
  * Do "smart" method look up as much as possible

### Rewrite Parser in ANTLR ###
Rewrite the parser in [ANTLR](http://www.antlr.org). Possible benefits:

  * Clearer grammar definition, auto-exportable to BNF for documentation
  * Build a parse tree usable by other tools
  * Better, more consistent, error handling

### Port SML to jsoar ###
Implement the classes in sml.jar with the jsoar API directly. May provide a drop-in switch from csoar to jsoar for existing projects. Issues:

  * XML tracing
    * This is ugly namely because we haven't come up with a clean way to generate the XML data from the kernel (the print statements are so much simpler in general, but also don't contain as much information).
  * ElementXML?
    * It's become clear that the function of ElementXML is to assist in transferring data between the sever and client sides, and the fact that it's "XML" is not actually a requirement. Other representations should be explored (possibly Google's protobuf). If we're not doing remote connections (see below), then maybe we don't need this at all and can just make direct function calls.
  * Support for remote connections?
    * The primary driver for remote connections in csoar is to support connecting and disconnecting the debugger at will (so we don't incur the considerable overhead of running it if we don't need it). It has been used on occasion to actually run SML clients remotely. If SML properly supported multiple input-producing clients, that would probably also be a popular use. If there is a way to load/unload the debugger jar at will, then perhaps that would accomplish most of what we use remote connections for.
  * Make run (optionally) non-blocking?
    * Making run in SML blocking has generally been considered a mistake. Of course, making it non-blocking would break a lot of existing code, but perhaps the behavior can be switched with an optional parameter or something (so by default it's blocking, but it can be made non-blocking if desired). Then we can change the default in the future when current code has caught up.

### Dynamic Editing Environment ###
Do something like [subtext](http://subtextual.org/) with jsoar. Graphically edit productions and store them in a database (rete?) rather than text files. As user edits productions, they are modified in a live kernel. Show matches and stuff on the fly rather than waiting to load them into an agent. Build "by-example" tests on the fly.

### Keep up with new Soar modules ###
Laird's students are working on modules in varying states of readiness for real use. We should keep up with these as they are released until an official switch to jsoar can be achieved. Episodic and semantic memory are probably the next in line.

  * Use JavaDB for episodic memory instead of whatever it's using now (SQLite?)

## Wouldn't it be fun if ... 7/21/2010 ##

  * Finish Bob's emo stuff, and port to JSoar.
  * Jane.
    * Twitter
    * Wikipedia
    * DBpedia
    * Semantic Web stuff (RDF)
    * Digg/Reddit
    * Statistics: Weather feeds, Earthquake feeds, Crime, Pirates
    * Where do Jane's goals come from?
  * NLP
    * Generate text (poetry) with tree adjoining grammars in Soar
  * System call interface
  * Integrate JSoar with Sim Jr
  * General method for attaching Soar to a data source (Cyc)
    * Databases
    * RDF
  * Massive multi-agent simulations.
  * Generic agent-messaging framework
  * Soar web framework. Implement web services, web pages, etc with a Soar agent
  * Agents cooperate to spell "poo" on the surface of a globe, such as the earth.
  * Working memory activation