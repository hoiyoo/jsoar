# Introduction #

Legilimens is a web-based debugging interface for JSoar agents. Once it is enabled in an application, it will start a lightweight web server through which you can monitor and debug your agents. From the web interface you can

  * View the agent trace
  * Execute commands
  * View and edit production
  * Inspect working memory
  * etc.

# Requirements #
To use Legilimens, **jsoar-legilimens.jar** must be on your class path. Additionally, Legilimens is only compatible with `ThreadedAgent`-based systems. The raw `Agent` class does not provide the concurrency guarantees required to build an effective general debugging interface.

# Enabling Legilimens #
To enable Legilimens you need only call the method `org.jsoar.legilimens.LegilimensServer.start()`. Once called, Legilimens will automatically detect new agents and hook into them as needed.

Alternatively, if the auto-start system property (see below) is enabled, Legilimens will be started when the first `ThreadedAgent` is constructed.

# Configuration #
Legilimens is mostly configured through system properties:

| **Property** | **Default** | **Description** |
|:-------------|:------------|:----------------|
| jsoar.legilimens.autoStart | false | If `true`, then Legilimens will be started automatically when the first `ThreadedAgent` instance is created. If Legilimens is not on the classpath, a warning is printed and this property is ignored. |
| jsoar.legilimens.root | "/jsoar" | The "application root" of the web-app. For example, the default value makes the web-app accessible at "http://localhost:12122/jsoar" |
| jsoar.legilimens.port | 12122 | The default port that the web server listens on. |
| jsoar.legilimens.trace.size | 256000 | The default size, in characters, of the in-memory trace buffer. The full trace is stored on disk, but the most recent agent output is stored in memory to improve performance. |
| jsoar.legilimens.trace.deleteOnExit | true | If true, the on-disk version of the trace will be deleted when the JVM exits. |