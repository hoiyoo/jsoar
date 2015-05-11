# Introduction #

The Soar kernel uses a custom cons list wherever singly linked lists are needed. In jsoar, I have generally converted these to instances of Java's [LinkedList](http://java.sun.com/javase/6/docs/api/java/util/LinkedList.html) class. I did a simple analysis of the speed of this class versus a custom list implementation (ListHead). I was most interested in iteration speed.

# Experiment Description #
These lists are usually pretty short (lists of variables, lists of tests and conditions, etc) so, I wrote a simple test that does the following:

  * Allocate the list
  * Add 20 integers (push for LinkedList, add for ArrayList)
  * Iterate over the integers
  * Repeat 10,000,000 times

I ran this test for the following cases:

  * LinkedList with foreach iteration
  * LinkedList with indexed iteration
  * ArrayList with foreach iteration
  * ArrayList with indexed iteration
  * ListHead with foreach iteration
  * ListHead with usual C-style linked list traveral

I also did each test with no iteration, just allocating and filling the lists.

# Results #
Here are the results:

| List Type | Iteration Type | Time |
|:----------|:---------------|:-----|
| LinkedList | foreach | 9.9s |
| LinkedList | indexed | 10.2s |
| LinkedList | none| 5.6s |
| ArrayList | foreach | 15.4s |
| ArrayList | indexed | 8.3s |
| ArrayList | none | 6.4s |
| ListHead | foreach | 8.6s |
| ListHead | C-style linked lists | 5.0s |
| ListHead | none | 4.8s |

Interestingly, the allocation time (no iteration) for the three containers is pretty comparable. ArrayList is a bit slower, but may show better results on longer lists, i.e. the test may be dominated by the array allocation.

However, when iteration is taken into account, we see that iterators really slow things down. Performance is again comparable in the foreach cases (ArrayList obviously should win on indexed iteration), but C-style linked list traversal with ListHead is an order of magnitude faster than the iterator.

# Conclusion #
Although ListHead is not appropriate for library code or public APIs, for the internals of the kernel, it may provide a significant performance increase over LinkedList.