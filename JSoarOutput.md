See also [JSoarEvents](JSoarEvents.md), [JSoarOutput](JSoarOutput.md)
# Introduction #

  * Register for `OutputEvent` (see also [JSoarEvents](JSoarEvents.md))
  * When the event fires, get output commands from `InputOutput.getPendingCommands()` (or handle WME changes in `OutputEvent` directly)
  * Use `Wmes.matcher()` to extract data from the output command working memory structures.

# Core Output #

# Output Commands #

# Handling Output Commands with SoarBeans #
JSoar has built-in support for automatically converting working memory to Java object, called SoarBeans. The basic idea is that you define a Java class, the bean, and then register an output command handler with `SoarBeanOutputManager`. When an output command is detected, the Soar working memory elements will be converted to an instance of your class and passed to a handler method you provide.

An example will make this clearer. Suppose you have a command called `move-to-point` which takes speed and location parameters:

```
^output-link
   ^move-to-point
      ^speed 2
      ^location
         ^x 45
         ^y 99
```

Processing this output command with `Wmes.matcher()` isn't too, bad, but is still tedious. With SoarBeans, we'll add a new class:

```
import java.awt.Point;

public class MoveToPoint
{
    public int speed;
    public Point location = new Point();
}
```

and then construct an output manager and register a handler:

```
final SoarBeanOutputManager manager = new SoarBeanOutputManager(agent.getEvents());
final SoarBeanOutputHandler<MoveToPoint> handler = new SoarBeanOutputHandler<MoveToPoint>() {

    @Override
    public void handleOutputCommand(SoarBeanOutputContext context, MoveToPoint bean)
    {
        // ... do something with bean.speed, bean.location.x, etc ...
        context.setStatus("complete");
    }};
manager.registerHandler("move-to-point", handler, MoveToPoint.class);
```

That's it. Some other random facts:

  * Beans can consist of primitive types, arrays, or other beans. Cycles and shared structure are supported.
  * A bean must have a public, no-arg constructor.
  * A bean property must either be public, or provide a setter method.
  * SoarBeans makes an effort to autoconvert, e.g. int to string, etc.
  * Simple name conversion is supported, e.g. `my-name` in Soar, becomes `myName` in Java.