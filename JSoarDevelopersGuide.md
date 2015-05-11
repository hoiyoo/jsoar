# Introduction #
The instructions here are only if you're interested in checking out JSoar and building it from source, or contributing to the JSoar source code. To use JSoar in a project, just grab the jars from a release and see [JSoarUsersGuide](JSoarUsersGuide.md)

# Software Requirements #
JSoar has the following software requirements:

  * Java 1.6 - jsoar [uses features from Java 1.6](Java16FeaturesUsedByJSoar.md). If this is not an option, contact jsoar-dev@googlegroups.com and we'll see what we can do.

# Coding Conventions #
The coding conventions for the JSoar codebase are stored as Eclipse formatter rules in **[jsoar/eclipse-formatter.xml](https://jsoar.googlecode.com/git/eclipse-formatter.xml)**.

To import into Eclipse:

```
Window -> Preferences -> Java -> Code Style -> Formatter -> Import ...
```

Otherwise, basic rules are:

  * NO TABS
  * 4 spaces of indentation
  * Opening braces on their own line
  * WRITE TESTS
  * Document public methods


# Checking out into Eclipse #

JSoar has been developed in Eclipse so the easiest way to get started modifying the code is with Eclipse.  You have two options for checking out the code.

**Option 1 (somewhat recommended, but YMMV):** The first option is with the !EGit Eclipse Team provider for the Git version control system http://www.eclipse.org/egit/ (http://download.eclipse.org/egit/updates). This method is highly recommended. Once !EGit is installed:

  * File -> Import ... -> Git -> Projects from Git -> Clone...
  * URI: https://code.google.com/p/jsoar/

Follow the wizard (accept default options), checking out all projects in the workspace. Note that, by default, EGit puts the repository in a folder dictated by Team -> Git -> Default Repository Folder. They do not recommend cloning projects in to the workspace.

**Option 2:** The other option is some other Git tool like the command-line or TortoiseGit. In this case, once the code is checked out from https://code.google.com/p/jsoar/

  * File -> Import ... -> General -> Existing Projects into Workspace

Follow the wizard, selecting the jsoar-core, jsoar-debugger, etc projects, to pull the projects into Eclipse.

That's all that's necessary. Eclipse will automatically build the code and all library dependencies are already in the source tree.

# Running the code #
There are already Eclipse launch configurations set up in the tools/launches folder. To run one, click Run -> Run Configurations -> Java Applications and select an app to run. It is recommended that you duplicate one of the existing launch configurations if you need your own.

To run the unit tests (please do so you know you haven't broken anything), click Run -> Run Configurations -> JUnit -> jsoar-core - All Unit Tests. If the bar in Eclipse is green, everything is good. New unit tests go in the src/test/java folder or the appropriate sub-project.

# Building a release #
To build a release (jars) run ANT on build.xml in the jsoar-build project. In Eclipse, Run -> External Tools -> jsoar-build - Build All. Otherwise, from a command line (make sure ANT is on your path):

```
$ cd jsoar
$ ant
```

In either case, a distribution will be created in jsoar-build/dist. It will automatically version it as "DATE-snapshot". To give it a version number:

```
$ ant -Dversion=X.X.X
```

# Extending JSoar #
TODO