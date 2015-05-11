#summary Java RHS function (deprecated see [JSoarScripting](JSoarScripting.md))
#labels Deprecated

# Introduction #

_As of August 25, 2010 (JSoar 0.10.1), the **java** RHS function is no longer available. Use [JSoarScripting](JSoarScripting.md) support instead._

This page describes the basic operation of the **java** RHS function which allows Soar agents to call arbitrary Java code from the right hand side of rules. See the JavaDoc comments in [org.jsoar.kernel.rhs.functions.JavaRhsFunction](http://code.google.com/p/jsoar/source/browse/trunk/jsoar/src/org/jsoar/kernel/rhs/functions/JavaRhsFunction.java) for a more detailed spec.

_**Note:** The current implmementation of this feature makes use of the JavaScript engine included with Java SE 6. On Mac OS X, this feature is not included by default. [This page](http://jmesnil.net/weblog/2008/05/14/how-to-include-javascript-engine-in-apples-java-6-vm) describes the process of installing this feature on OS X._

The **java** RHS function always takes a first argument indicating which mode it will execute in. Each mode is described below.

## Calling a Static Method ##
To call a static method, use the **static** mode:
```
(java static <class+method> args...)
```
For example:
```
(java static |java.lang.Math.abs| -1)  #  Math.abs(-1);
```

Or refer to a static member:
```
(java get <class+member>)
```
For example:
```
(java static java.lang.Math.PI)  # Math.PI;
```

## Calling an Non-Static Method ##
To call a non-static method, use the **method** mode:
```
(java method <object> <method> args...)
```
For example:
```
(java method <o> toString)  # o.toString();
```

Or refer to a non-static member:
```
(java get <object> <member>)
```
For example:
```
(java member <o> foo)      # o.foo
```

## Calling a Constructor ##
To construct a new object, use the **new** mode:
```
(java new <class> args...)
```
For example:
```
(java new String |Value of the new string|)
```

## Referring to null ##
To use pass null to a function:
```
(java static (java static System out) (java null))  # System.out.println(null);
```