The following Java 1.6 features are used by jsoar, preventing it to be run on earlier JVMs:

  * LinkedList.push()
  * LinkedList.pop()
  * ServiceLoader
  * ScriptEngineManager - used by JavaRhsFunction

That's as far as I got. Note that since these are all changes to the class libraries rather than language changes, it should be fairly straightforward, if tedious, to port back to 1.5 if necessary.