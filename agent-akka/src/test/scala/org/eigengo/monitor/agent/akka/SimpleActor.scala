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
