# System Properties #
There are a few system properties that control the behavior of jsoar. These generally correspond to pre-processor macros in the original C implementation of Soar:

| **Property** | **Default Value** | **Notes** |
|:-------------|:------------------|:----------|
| jsoar.agent.interpreter | default | Select the command interpreter used for newly created agents. For example, if you have jsoar-tcl.jar on the classpath, setting this property to "tcl" will enable Tcl process of Soar code and the **tcl** right-hand-side function. |
| jsoar.do\_top\_level\_ref\_cts | false | See org.jsoar.kernel.SoarConstants |
| jsoar.o\_rejects\_first | true | See org.jsoar.kernel.SoarConstants |
| jsoar.debugger.provider | org.jsoar.debugger.DefaultDebuggerProvider | See org.jsoar.kernel.DefaultDebuggerProvider |
| jsoar.discardVarNames | false | If true, var names will not be preserved with rules. Depending on the size of the rule base, this may provide some memory savings. When rules are printed out, the original variable names will be lost, replaced by generated names |
| jsoar.warnOnJavaSymbols | true | If true, a warning is printed whenever a [JavaSymbol](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/symbols/JavaSymbol.java) is created with the [Symbols.create()](http://code.google.com/p/jsoar/source/browse/jsoar-core/src/main/java/org/jsoar/kernel/symbols/Symbols.java) helper method. This can be a source of bugs and confusion so the warning is on by default. You can disable it if you know what you're doing. The message will print something like _"WARNING: A Java symbol with value 'java.awt.Point[x=123,y=456]' is being created. Are you sure this is what you want to do? Disable this message with -Djsoar.warnOnJavaSymbols=false."_ |