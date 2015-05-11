#summary JSoar Debugger
#labels Featured

# Introduction #

JSoar includes a graphical debugger implemented directly with the JSoar Java API and Swing. It is included in jsoar-debugger-X.X.X.jar and is launched by default when the JSoar startup shell script (jsoar.bat on Windows) is run. Here's a screenshot:

![http://wiki.jsoar.googlecode.com/git/jsoar-debugger.png](http://wiki.jsoar.googlecode.com/git/jsoar-debugger.png)

# Views #
The debugger consists of a number of "views", each of which is a separate dockable windows within the overall debugger window. Each view provides a different set of information about the agent being debugged. Most views are shown by default. Other, less commonly used views can be opened through the **View** menu.

Many views are driven by **selection**. That is, selection an item in one view will cause another view to update its contents. For example, selecting a WME in the trace view, will cause the WME support view to show support information about that WME. Each of the views is described in more detail below.

## Trace View ##
The trace view shows the traditional trace of the agent's execution. It also shows the output of commands executed. What's printed in the trace is controlled by the **watch** command. You can also right-click on the trace window and control what's printed.

![http://wiki.jsoar.googlecode.com/git/debugger/trace-view.png](http://wiki.jsoar.googlecode.com/git/debugger/trace-view.png)

The trace view has several useful capabilities:

  * Clicking on a production name in the trace text makes that production the current selection. This will update other views that show production information, like the **partial matches** view.
  * Clicking on a WME attribute (for example, in the output of the **print** command) in the trace text makes that WME the current selection. This will update other view that show WME information, like the **WME support** view.
  * Clicking on an identifier makes that identifier the current selection.
  * Right-click a production, WME, or identifier for more options (same as CSoar debugger)
  * Double-click an identifier or WME in the trace text to drill down. This is equivalent to executing the print command. For example, double-clicking "S1" in the trace text is the same as executing "print S1"
  * Double-click an production in the trace text to print the production. This is equivalent to executing the print command. For example, double-clicking "apply\*foo" in the trace text is the same as executing "print apply\*foo"

## Production View ##
The production view shows a list of all production rules currently loaded in the agent. The list can be filtered with a regex. Selecting one or more productions will change the current selection. The tooltip for a single rule shows the rule name, its documentation strings and its firing count.

![http://wiki.jsoar.googlecode.com/git/debugger/productions-view.png](http://wiki.jsoar.googlecode.com/git/debugger/productions-view.png)

## Goal Stack View ##
The goal stack view shows the current goal stack including currently selected operators if any.

  * Clicking on a state or operator makes it the current selection.
  * Double-clikcing a state or operator will print it out.

![http://wiki.jsoar.googlecode.com/git/debugger/goal-stack-view.png](http://wiki.jsoar.googlecode.com/git/debugger/goal-stack-view.png)

## Match Set View ##
The match set view shows the current match set. This is equivalent to executing the **matches** command will no arguments. The top half of the view shows rules that are about to fire or retract. If a rule in the top half is selected, the WMEs associated with that rule will be shown in the bottom half of the view.

![http://wiki.jsoar.googlecode.com/git/debugger/match-set-view.png](http://wiki.jsoar.googlecode.com/git/debugger/match-set-view.png)

## Partial Matches View ##
The partial matches view shows partial matches for the current selected production rule(s) (a rule can be selected in the trace view, production view, etc). This is equivalent to executing the **matches** command on the name of the fule. Matching conditions are shown in green, while non-matching conditions are shown in red, and marked with a >> indicator. The view will automatically update as the agent is stepped in the debugger.

![http://wiki.jsoar.googlecode.com/git/debugger/partial-matches-view.png](http://wiki.jsoar.googlecode.com/git/debugger/partial-matches-view.png)

## Preferences View ##
The preferences view shows operator preferences for the bottom-most state. It's equivalent to the **preferences** command.

![http://wiki.jsoar.googlecode.com/git/debugger/preferences-view.png](http://wiki.jsoar.googlecode.com/git/debugger/preferences-view.png)

## WME Support View ##
The WME support view shows the rule instantiations that support the currently selected WME (a WME can be selected in the trace view, WME search view, etc). Selecting a rule in the top half of the view will show the WMEs that caused that rule to match in the bottom half of the view.

![http://wiki.jsoar.googlecode.com/git/debugger/wme-support-view.png](http://wiki.jsoar.googlecode.com/git/debugger/wme-support-view.png)

## WME Search View ##
The WME search view allows working memory to be filtered by glob patterns on id, attribute and value fields. It can be opened from the **View** menu. Once open, just enter glob patterns in the three text fields and click the **Search** button. Selecting one or more WMEs in this view will change the current selection.

In this screenshot, all WMEs with id "S1" are displayed:

![http://wiki.jsoar.googlecode.com/git/debugger/wme-support-view.png](http://wiki.jsoar.googlecode.com/git/debugger/wme-support-view.png)