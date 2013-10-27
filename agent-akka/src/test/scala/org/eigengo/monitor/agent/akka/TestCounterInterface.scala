package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.output.CounterInterface
import scala.collection.mutable

/**
 * Test-only implementation of the ``CounterInterface`` that records all received
 * counters, gauges and others in its companion object, which you can then use
 * in your tests to make assertions.
 */
class TestCounterInterface extends CounterInterface {

  override def decrementCounter(name: String, tags: String*): Unit = {
    TestCounterInterface.add(name, -1, tags.toList)
  }

  override def incrementCounter(name: String, tags: String*): Unit = {
    TestCounterInterface.add(name, 1, tags.toList)
  }

  override def recordGaugeValue(aspect: String, value: Int, tags: String*): Unit = {
    TestCounterInterface.set(aspect, value, tags.toList)
  }
}

/**
 * An entry received by the counter interface, for example _statsd_
 *
 * @param aspect the counter aspect (i.e. group name)
 * @param value the counter value
 * @param tags the optional tags
 */
case class TestCounter(aspect: String, value: Int, tags: List[String] = Nil)
object TestCounter {
  // asserts that the two counters are compatible
  private def assertAspects(a: TestCounter, b: TestCounter): Unit = {
    if (a.aspect != b.aspect) throw new RuntimeException(s"${a.aspect} != ${b.aspect}")
  }

  /**
   * Adds the values of two counters
   *
   * @param a the first counter
   * @param b the second counter
   * @return a + b
   */
  def plus(a: TestCounter, b: TestCounter): TestCounter = {
    assertAspects(a, b)
    a.copy(value = a.value + b.value)
  }

  /**
   * Returns the greater of the two counters
   *
   * @param a the first counter
   * @param b the second counter
   * @return the greater of {a, b}
   */
  def max(a: TestCounter, b: TestCounter): TestCounter = {
    assertAspects(a, b)
    if (a.value > b.value) a else b
  }

}

/**
 * Specifies tag filtering to be either AnyTag, SingleTag
 */
sealed trait TagFilter
case object AnyTag extends TagFilter
case class ExactTag(tag: String) extends TagFilter
case class ContainsTag(tag: String) extends TagFilter

/**
 * Companion for the TestCounterInterface containing all recorded events
 */
object TestCounterInterface {
  private val counters = new mutable.ListBuffer[TestCounter]()

  // adds a new counter
  private def add(aspect: String, delta: Int, tags: List[String]): Unit = {
    val counter: TestCounter = TestCounter(aspect, delta, tags)
    counters += counter

    println(s"**counter: $aspect -> $counter")
  }

  // sets a gauge
  private def set(aspect: String, value: Int, tags: List[String]): Unit = {
    val gauge = TestCounter(aspect, value, tags)
    counters += gauge

    println(s"**gauge:   $aspect -> $gauge")
  }

  /**
   * Selects from the recorded counters those with the matching ``aspect`` and conforming to the
   * requested ``tagFilter``, and then folds the matches over some operation ``fold``.
   *
   * @param aspect the aspect to be matched
   * @param tagFilter the tag filter
   * @param fold the fold operation
   * @return list of matching & folded tags
   */
  def foldlByAspect(aspect: String, tagFilter: TagFilter = AnyTag)(fold: (TestCounter, TestCounter) => TestCounter): List[TestCounter] = {
    def pred(x: TestCounter): Boolean =
      (x.aspect == aspect) &&
      (tagFilter match {
        case AnyTag           => true
        case ExactTag(tag)    => x.tags.size == 1 && x.tags.head == tag
        case ContainsTag(tag) => x.tags.contains(tag)
      })

    counters.foldLeft[List[TestCounter]](Nil) { (b, a) =>
      if (a.aspect == aspect) b.find(pred).map(fold(a, _)).getOrElse(a) :: b else b
    }
  }

  /**
   * Returns immutable view of all recorded counters
   *
   * @return all recorded counters
   */
  def allCounters: List[TestCounter] = counters.toList

}