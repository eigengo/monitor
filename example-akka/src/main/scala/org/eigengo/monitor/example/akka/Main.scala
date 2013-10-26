package org.eigengo.monitor.example.akka

import akka.actor.{Props, ActorSystem, Actor}

// run with -javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
// in my case -javaagent:/Users/janmachacek/.m2/repository/org/aspectj/aspectjweaver/1.7.3/aspectjweaver-1.7.3.jar
object Main extends App {

  class SampleActor extends Actor {
    def receive: Receive = {
      case i: Int if i > 0 =>
        println(s"Counting down... Now $i")
        Thread.sleep(100)
        self ! (i - 1)
      case i: Int if i == 0 =>
        println("Done.")
    }
  }

  val system = ActorSystem()
  val sample = system.actorOf(Props[SampleActor])
  val CountPattern = "(\\d+)".r

  def commandLoop(): Unit = {
    Console.readLine() match {
      case "quit"          => return
      case CountPattern(i) => sample ! (i.toInt * 10)
      case _               => println("WTF?")
    }

    commandLoop()
  }

  commandLoop()
  system.shutdown()

}
