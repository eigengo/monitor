package org.eigengo.monitor.agent.akka

object Aspects {
  val deliveredInteger = "akka.actor.delivered.Integer"
  val deliveredString  = "akka.actor.delivered.String"
  val undelivered      = "akka.actor.undelivered"
  val queueSize        = "akka.queue.size"
  val actorDuration    = "akka.actor.duration"
  val actorError       = "akka.actor.error"
  val actorCount       = "akka.actor.count"

}
