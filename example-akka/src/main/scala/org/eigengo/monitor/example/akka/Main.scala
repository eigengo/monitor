package org.eigengo.monitor.example.akka

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.routing.RoundRobinRouter

// run with -javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
// in my case -javaagent:/Users/janmachacek/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
object Main extends App {

  class FooActor(bar: ActorRef) extends Actor {
    def receive: Receive = {
      case i: Int if i > 0 =>
        println(s"Counting down... Now $i")
        Thread.sleep(100)
        if (i % 10 == 0) bar ! i
        self ! (i - 1)
      case i: Int if i == 0 =>
        println("Foo done.")
    }
  }

  class BarActor extends Actor {
    def receive: Receive = {
      case i: Int if i > 0 =>
        Thread.sleep(10)
        self ! (i - 1)
      case i: Int if i == 0 =>
        println("Bar done.")
    }
  }

  val system = ActorSystem()
  val bar = system.actorOf(Props[BarActor].withRouter(RoundRobinRouter(nrOfInstances = 10)), "bar")
  val foo = system.actorOf(Props(new FooActor(bar)), "foo")
  val CountPattern = "(\\d+)".r

  def commandLoop(): Unit = {
    Console.readLine() match {
      case "quit"          => return
      case "go"            => (0 to 20).foreach(_ => foo ! 400)
      case CountPattern(i) => foo ! (i.toInt * 10)
      case _               => println("WTF?")
    }

    commandLoop()
  }

  commandLoop()
  system.shutdown()

}
