package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.output.CounterInterface
import scala.collection.mutable

class TestCounterInterface extends CounterInterface {

  def decrementCounter(name: String, tags: String*): Unit = {
    TestCounterInterface.add(name, -1, tags.toList)
  }

  def incrementCounter(name: String, tags: String*): Unit = {
    TestCounterInterface.add(name, 1, tags.toList)
  }

  def recordGaugeValue(aspect: String, value: Int, tags: String*): Unit = {
    TestCounterInterface.set(aspect, value, tags.toList)
  }
}

case class TestCounter(value: Int, tags: List[String] = Nil) {
  def +(delta: Int): TestCounter = TestCounter(value + delta, tags)
}

object TestCounterInterface {
  val counters = new mutable.HashMap[String, TestCounter]()

  def add(aspect: String, delta: Int, tags: List[String]): Unit = {
    val newCount = counters.getOrElse(aspect, TestCounter(0, tags)) + delta
    counters.put(aspect, newCount)

    println(s"**counter: $aspect -> $newCount")
  }

  def set(aspect: String, value: Int, tags: List[String]): Unit = {
    val gauge = TestCounter(value, tags)
    counters.put(aspect, gauge)
    println(s"**gauge:   $aspect -> $gauge")
  }

}