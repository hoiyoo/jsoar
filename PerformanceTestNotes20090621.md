# Introduction #
See also: JSoarPerformance

I ran some performance tests between JSoar and CSoar. Here's the relevant version information:

  * Intel Core2 Duo T5700, 2.19 GHz, 4GB RAM (3.0GB reported in Windows)
  * Windows XP SP3, 32-bit
  * !JSoar trunk [revision 417](https://code.google.com/p/jsoar/source/detail?r=417) on JDK 1.6.0\_11
  * !CSoar 9.0.1 RC4 release by Jon Voigt 6/11/2009

I ran three separate tests.

## FunctionalTests\_testTowersOfHanoi.soar ##
```
JSoar
client
   CPU: min 0.587262, med 0.596783, max 0.840917
Kernel: min 0.566307, med 0.575579, max 0.817097

server
   CPU: min 0.346169, med 0.366038, max 1.733401
Kernel: min 0.325481, med 0.345569, max 1.700595
----
CSoar
Kernel Total
0.140  0.359
0.232  0.359
0.219  0.360
```

## FunctionalTests\_testArithmetic.soar ##
```
JSoar
client
   CPU: min 6.124161, med 6.201587, max 7.962679
Kernel: min 5.618026, med 5.689400, max 7.429138

server
   CPU: min 4.568763, med 4.950035, max 8.500420
Kernel: min 4.083870, med 4.470613, max 7.909352

CSoar
Kernel Total
1.903  4.234
2.136  4.392
2.114  4.127
```

## count-test-single.soar ##
```
JSoar
client
   CPU: min 8.570688, med 8.750060, max 9.713601
Kernel: min 7.877735, med 8.067715, max 8.985043

server
   CPU: min 6.590305, med 6.841578, max 11.452824
Kernel: min 5.944954, med 6.196614, max 10.504368

CSoar
Kernel Total
4.065  6.204
4.003  6.595
4.005  6.395
```