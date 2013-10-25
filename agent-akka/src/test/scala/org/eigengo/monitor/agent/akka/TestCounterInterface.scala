package org.eigengo.monitor.agent.akka

import org.eigengo.monitor.output.CounterInterface
import scala.collection.mutable

class TestCounterInterface extends CounterInterface {

  def decrementCounter(name: String): Unit = {
    TestCounterInterface.add(name, -1)
  }

  def incrementCounter(name: String): Unit = {
    TestCounterInterface.add(name, 1)
  }

}

object TestCounterInterface {
  val counters = new mutable.HashMap[String, Int]()

  def add(name: String, delta: Int): Unit = {
    val newCount = counters.getOrElse(name, 0) + delta
    counters.put(name, newCount)
  }

}