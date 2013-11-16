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
package org.eigengo.monitor.agent.akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Inbox;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class JavaApiAkkaApplication {
    public static final String ACTOR_SYSTEM_NAME = "javaapi";

    public static class Greet implements Serializable {}
    public static class WhoToGreet implements Serializable {
        public final String who;

        public WhoToGreet(String who) {
            this.who = who;
        }
    }
    public static class Greeting implements Serializable {
        public final String message;

        public Greeting(String message) {
            this.message = message;
        }
    }

    public static class Greeter extends UntypedActor {
        private String greeting = "";

        public void onReceive(Object message) {
            if (message.toString().equals("die")) context().stop(self());
            else if (message instanceof WhoToGreet) greeting = "hello, " + ((WhoToGreet) message).who;
            // Send the current greeting back to the sender
            else if (message instanceof Greet) getSender().tell(new Greeting(greeting), getSelf());
            else unhandled(message);
        }
    }

    public static class GreetPrinter extends UntypedActor {
        public void onReceive(Object message) {
            if (message.toString().equals("die")) context().stop(self());
            else if (message instanceof Greeting) System.out.println(((Greeting) message).message);

        }
    }

    public static ActorSystem create() {
        // Create the 'helloakka' actor system
        final ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);

        // Create the 'greeter' actor
        final ActorRef greeter = system.actorOf(Props.create(Greeter.class), "greeter");

        // Create the "actor-in-a-box"
        final Inbox inbox = Inbox.create(system);

        // Tell the 'greeter' to change its 'greeting' message
        greeter.tell(new WhoToGreet("akka"), ActorRef.noSender());

        // Ask the 'greeter for the latest 'greeting'
        // Reply should go to the "actor-in-a-box"
        inbox.send(greeter, new Greet());

        // Change the greeting and ask for it again
        greeter.tell(new WhoToGreet("typesafe"), ActorRef.noSender());
        inbox.send(greeter, new Greet());

        system.actorOf(Props.create(GreetPrinter.class), "greetPrinter");

        return system;
    }

}