JSoar is a pure Java implementation of the Soar kernel. See 
https://github.com/soartech/jsoar/wiki for more information.

See jsoar-build/readme.txt for build instructions

*** Coding Conventions ***

The coding conventions for the JSoar codebase are stored as Eclipse
formatter rules in eclipse-formatter.xml. To import:

Window -> Preferences -> Java -> Code Style -> Formatter -> Import ...

The basic rules are:

* NO TABS
* 4 spaces of indentation
* Opening braces on their own line
* Line endings should be LF in the git repo
** Line endings are not handled by the Eclipse formatter
** CRLF endings can be used by turning autocrlf on in git
** For more information about how git handles line endings see http://adaptivepatchwork.com/2012/03/01/mind-the-end-of-your-line/

Acknowledgements / History
JSoar was originally envisioned and implemented by Dave Ray (and indeed, the 
vast majority of the code is still Dave's). JSoar started out on googlecode in
svn, was converted to mercurial and then git, and then moved to Dave's github 
site, https://github.com/daveray/. Today JSoar is primarily maintained by Soar 
Technology, Inc.