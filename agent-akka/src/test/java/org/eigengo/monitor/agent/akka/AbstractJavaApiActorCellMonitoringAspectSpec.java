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

import akka.actor.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Exercises Akka's Java API for testing.
 */
abstract class AbstractJavaApiActorCellMonitoringAspectSpec {
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
            else if (message instanceof WhoToGreet) this.greeting = "hello, " + ((WhoToGreet) message).who;
            else if (message instanceof Greet) getSender().tell(new Greeting(this.greeting), getSelf());
            else unhandled(message);
        }
    }

    public static class GreetPrinter extends UntypedActor {
        public void onReceive(Object message) {
            if (message.toString().equals("die")) context().stop(self());
            else if (message instanceof Greeting) System.out.println(((Greeting) message).message);

        }
    }

    private class OuterActor extends UntypedActor {

        private final InnerActorCreator innerActorCreator = new InnerActorCreator();

        public void onReceive(Object message) {
            if (message instanceof UUID) {
                innerActorCreator.create((UUID) message);
            }
        }

        private class InnerActorCreator {
            InnerActorCreator(){}
            Props innerActorProps(UUID uuid) {return Props.create(InnerActor.class, uuid);}
            void create(UUID uuid) {
                context().actorOf(innerActorProps(uuid));
            }
        }

        class InnerActor extends UntypedActor {
            final UUID id;
            InnerActor(UUID uuid) {
                this.id = uuid;
            }
            public void onReceive(Object message) {
                sender().tell(id, self());
            }
        }
    }

    // The ActorSystem under test
    protected final ActorSystem system;
    // The following fields are the Props used to create the matching ActorRefs below
    protected final Props greeterProps;
    protected final Props greetPrinterProps;
    protected final Props unnamedGreetPrinterProps;
    protected final Props outerActorProps;
    // The following fields are the ActorRefs constructed using the Props above
    protected final ActorRef greeter;
    protected final ActorRef greetPrinter;
    protected final ActorRef unnamedGreetPrinter;
    protected final ActorRef outerActor;

    /**
     * Constructs the ActorSystem under test, and creates the Props and ActorRefs above
     */
    public AbstractJavaApiActorCellMonitoringAspectSpec() {
        // Create the actor system
        this.system = ActorSystem.create(ACTOR_SYSTEM_NAME);

        // Create the 'greeter' actor
        this.greeterProps = Props.create(Greeter.class);
        this.greeter = system.actorOf(this.greeterProps, "greeter");

        // Create the "actor-in-a-box"
        final Inbox inbox = Inbox.create(this.system);

        // Tell the 'greeter' to change its 'greeting' message
        this.greeter.tell(new WhoToGreet("akka"), ActorRef.noSender());

        // Ask the 'greeter for the latest 'greeting'
        // Reply should go to the "actor-in-a-box"
        inbox.send(this.greeter, new Greet());

        // Change the greeting and ask for it again
        this.greeter.tell(new WhoToGreet("typesafe"), ActorRef.noSender());
        inbox.send(this.greeter, new Greet());

        this.greetPrinterProps = Props.create(GreetPrinter.class);
        this.greetPrinter = system.actorOf(this.greetPrinterProps, "greetPrinter");

        this.unnamedGreetPrinterProps = Props.create(GreetPrinter.class);
        this.unnamedGreetPrinter = system.actorOf(this.unnamedGreetPrinterProps);

        this.outerActorProps = Props.create(OuterActor.class);
        this.outerActor = system.actorOf(this.outerActorProps);

    }

}