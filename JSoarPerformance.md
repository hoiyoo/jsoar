# Introduction #

For the community to seriously consider switching from csoar to jsoar, there can't be too much of a hit for using jsoar. Specifically, the performance hit has to be offset by the various benefits of jsoar (maintainability, cross language support, etc.). For this reason, we are tracking how the performance of jsoar compares to that of csoar as they both develop.

The results themselves are here:

http://spreadsheets.google.com/pub?key=pvVGDfR2QzuxxVyXkh3TxUQ

Instructions on how to test the performance of jsoar and csoar are below.

# Performance Testing Procedures #

NOTE: To be a fair comparison, the soar code used for testing shouldn't use RHS writes. For example, to run TOH, you should remove the monitor rule.

## Testing jsoar ##

  * build jsoar using the external build.xml from eclipse.
  * run dist/perftimer.bat <source file>
    * Note: this will run the soar program 15 times.

## Testing csoar ##

  * build csoar in release mode
  * SoarLibrary/bin/TestCLI <source file>
  * execute the following commands
    * watch 0
    * run
    * stats
    * (init-soar and repeat if desired. Unlike jsoar, there isn't much variability, so I usually only run 3 times)

# Reporting Results #
Currently, jsoar and csoar are both capable of reporting Total CPU and Kernel times. I've been recording Total CPU times in the spreadsheet above, but it shouldn't really matter what you report so long as it's the same between the two (do whatever comparison you like).

Here are the tables that may need to be updated in the spreadsheet:

  1. machines: If the testing machine is not listed, add it with a unique id.
  1. test\_data: Here is a description of the test run. If you are running a new test, add an entry with a unique id.
  1. run\_data: Here's data associated with a particular run, including the id of the machine it was run on, the date, the id of the test that was run, the specific revisions of csoar and jsoar, and any special options used when running jssoar or csoar.
  1. perf\_data: The actual performance numbers associated with a particular run id. Includes the median, min, and max for the test (each is run multiple times as per the procedures above).
    * Notes
      * Median is reported since jsoar speeds up as it runs repeatedly. If average were reported, it would be skewed by the first few runs.
      * Max is important for jsoar as it gives an idea of what performance will be like (relative to csoar) for the first run of a program.
      * perf\_data could be combined with run\_data in a single table (there's a one-to-one correspondence between their rows), but having a separate table makes it easy to see the numbers people care about.

# Fine Tuning Java #
It turns out that there are a lot of options that give you pretty fine control over how parts of Java work, especially the garbage collector. In particular, there are multiple garbage collectors with different properties. Java will attempt to select a collector automatically based on your application, machine, and other parameters (e.g., heap size), but you can force the selection of a particular collector using various -XX options (see the link below). In particular, the -XX:+UseConcMarkSweepGC sacrifices throughput for response time, and can significantly reduce "stuttering" (where the application appears to pause because garbage collection is going on in the background).

Finally, the heap size is the most significant factor in Java's performance. In general, you should give Java as much memory as possible, as this will reduce the amount of time it has to spend doing garbage collection (indeed, Java will take significantly more memory if you let it). If performance is critical, you can even set the min and max heap sizes the same, and Java won't waste time trying to resize it as the application's needs change.

There are a lot more details and options described here:
http://java.sun.com/javase/technologies/hotspot/gc/gc_tuning_6.html