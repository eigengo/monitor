/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.agent.akka

import akka.actor.Actor
import akka.japi.Creator

class SimpleActor extends Actor {

  def receive: Receive = {
    case s: String =>
    // do nothing
    case i: Int =>
      // for speed testing
      Thread.sleep(i)
    case 'stop =>
      context.stop(self)
    case false =>
      throw new RuntimeException("Bantha poodoo!")
  }

}

/**
 * Creator implementation to test Akka Java API
 */
class SimpleActorCreator extends Creator[SimpleActor] {
  def create(): SimpleActor = new SimpleActor
}

class WithUnhandledActor extends Actor {

  def receive: Receive = {
    case i: Int =>
  }

  override def unhandled(message: Any): Unit = {
    // eat my shorts
  }

}

trait NullTestingActor extends Actor {

  def receive: Receive = {
    case _ =>
  }

  override def unhandled(message: Any): Unit = ()

}

class NullTestingActor1 extends NullTestingActor

class NullTestingActor2 extends NullTestingActor

class NullTestingActor3 extends NullTestingActor

class KillableActor extends Actor {

  def receive: Receive = {
    case 'stop =>
      context.stop(self)
    case _ =>
  }

}
