#summary Soar Unit Testing Framework

_See also [this blog post](http://blog.darevay.com/2010/08/introducing-soarunit/)_

# Introduction #
_See screenshots at the bottom of this page_

SoarUnit is a framework for unit testing Soar code introduced in JSoar 0.10.1. It is implemented as part of JSoar, but it supports running code in either JSoar or CSoar 9.3.1. When run, SoarUnit scans a given directory for test cases (see below) and runs all of the tests that are found. It then reports the test results, either in a text format, or through a user interface similar to the JUnit view in Eclipse.

**SoarUnit was created to support development of the [Bebot](http://github.com/daveray/bebot) Soar library. There are [numerous test examples there](http://github.com/daveray/bebot/tree/master/src/test/soar/bebot). Note that the tests are only compatible with JSoar.**

**Example tests compatible with both JSoar and CSoar (for the waterjug demo) are available [here](http://wiki.jsoar.googlecode.com/hg/SoarUnitDemo.zip).**

# How Tests are Run #
The run loop of SoarUnit looks like this:

  * Create a new agent and `cd` to the directory containing the file of the current test
  * Source code in the `setup` block
  * Source code in the current `test` block
  * Run the agent and wait for the `(pass)` or `(fail)` RHS functions to be called (see below)
  * Record test results
  * Destroy the agent
  * Repeat

# Test Cases #
Currently a test case is identified by SoarUnit as a file that starts with "test" and ends with ".soar".

A SoarUnit test case consists of a `setup` block and one or more `test`s. The `setup` block can source any code needed to setup up the tests. As noted above, the setup block is executed before each test is run. Here's an example of a setup block:

```
setup {
  source ../common.soar

  sp {apply*init*add-state-name
    (state <s> ^operator.name init)
  -->
    (<s> ^name test-list-get)
  } 

  ...
```

Individual tests are specified with test blocks. Each test block has a name and a set of test-specific code that implements the test:

```
test "Test multiply multiplies value by factor" {
  sp {propose*identity
    (state <s> ^name test-functions
              -^result)
  -->
    (<s> ^operator <o>)
    (<o> ^name bebot*multiply
         ^value 99
         ^factor 3)
  }

  sp {pass
    (state <s> ^name test-functions
               ^result 297)
  -->
    (pass)
  }
} 
```

The rules in a test (and setup) have access to two new right-hand-side functions, `(pass)`, and `(fail)`. Both functions cause the test to halt. `(pass)` indicates the test has passed. There should always be a rule to test for success and call `(pass)`. On the other hand `(fail)` can be called by tests that detect a failure condition in the test. Both functions can take arbitrary arguments which are used to construct a message which is included in the test report.

# Code Coverage #
While running tests, SoarUnit keeps track of loaded rules and the firing counts of those rules. This information is used to perform a rudimentary code coverage calculation. Any rule that isn't fired counts against code coverage.

# Running SoarUnit #
SoarUnit is a command-line application which is run with **bin\soarunit.bat** on Windows and **bin\soarunit** on Unix systems. The simplest way to run SoarUnit is to point it at a directory:

```
$ soarunit /path/to/tests
```

this will run all tests found in the given directory. To run SoarUnit with CSoar rather than JSoar, set the `SOAR_HOME` environment variable and use the `--sml` switch:

```
$ export SOAR_HOME=/path/to/csoar-9.3.1
$ soarunit --sml /path/to/tests
```

Use the `--help` option for all command-line options.

**Known Issue: --sml only seems to work if -R is also specified.**

# Graphical User Interface #

SoarUnit has a graphical user interface which displays the familiar green bar from of other unit testing UIs. To run the run use the `--ui` option:

```
$ soarunit --ui /path/to/tests
```

Here's the UI with all tests passing:

![http://wiki.jsoar.googlecode.com/git/soarunit-pass.png](http://wiki.jsoar.googlecode.com/git/soarunit-pass.png)

Right-clicking a test gives options for editing and debugging a test.

Here's the UI with failing tests:

![http://wiki.jsoar.googlecode.com/git/soarunit-fail.png](http://wiki.jsoar.googlecode.com/git/soarunit-fail.png)

Here's the code coverage user interface:

![http://wiki.jsoar.googlecode.com/git/soarunit-fail.png](http://wiki.jsoar.googlecode.com/git/soarunit-fail.png)