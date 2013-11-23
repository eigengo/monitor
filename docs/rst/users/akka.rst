####
Akka
####

The Akka Monitor logs the following, always tagged by by the actor type, path and actor system name.
Specifically, we record:

* the number of messages received,
* the number of actors,
* the queue size,
* amount of time that a given actor spends in the ``receive`` call,
* number of exceptions thrown by an actor;

In addition to monitoring the actors, we monitor the thread pool's performance. For the
``ForkJoinPool`` and ``ThreadPoolExecutor``, we record:

* the number of active threads,
* the number or running threads,
* the number of queued tasks,
* the thread pool size.

The values are reported under *keys* or *aspects*, and decorated with *tags*. The *aspects* are

==================================  ========  ====================================================
Aspect/key                          Type      Description
==================================  ========  ====================================================
``akka.actor.delivered``            counter   the number of delivered messages to the actor
``akka.actor.undelivered``          counter   the number of undelivered messages to the actor
``akka.actor.queue.size``           gauge     the actor's mailbox size
``akka.actor.duration``             gauge     the time (in milliseconds) of the ``receive`` method
``akka.actor.error``                counter   the number of exceptions in the ``receive`` method
``akka.actor.count``                gauge     the number of actors
``akka.pool.thread.count``          gauge     the number of threads in the pool
``akka.pool.running.thread.count``  gauge     the number of active / running threads in the pool
``akka.pool.queued.task.count``     gauge     the number of queued tasks in the pool
``akka.pool.size``                  gauge     the thread pool size

The tags allow you to further identify the "source" of the gauge or counter. The tags identify the
specific actor path, actor type and actor system name. Consider the following code::

    package demo

    class DemoActor extends Actor {
      def receive: Receive = {
        case true  => Thread.sleep(10)
        case false => Thread.sleep(10); throw new RuntimeException("false")
      }
    }

    object Demo extends App {
      val system = ActorSystem("foo")
      val demo = system.actorOf(Props[DemoActor], "demo")
      demo ! true
      demo ! false
      demo ! "???"
    }

The tags for the ``demo`` actor will be ``akka.path://foo/user/demo``, ``akka.type:demo.DemoActor`` and
``akka.system:foo``. And we will record the following *aspects*:

* ``akka.actor.delivered`` -> 2 (The messages ``true`` and ``false``.)
* ``akka.actor.delivered.Boolean`` -> 2 (The messages ``true`` and ``false``.)
* ``akka.actor.undelivered`` -> 1 (The message ``"???"``.)
* ``akka.actor.undelivered.String`` -> 1 (The message ``"???"``.)
* ``akka.actor.queue.size`` -> min 0, max 3 (The ``Thread.sleep(10)`` and ``tell`` calls.)
* ``akka.actor.duration`` -> ~10 (Typically exactly 10, but could differ depending on switching.)
* ``akka.actor.error`` -> 1 (The ``throw new RuntimeException("false")``.)
* ``akka.actor.error.RuntimeException`` -> 1 (The ``throw new RuntimeException("false")``.)
* ``akka.actor.count`` -> 1 (The ``demo`` instance)
* ``akka.pool.thread.count`` -> 5 (Depending on the default configuration for the ``ActorSystem``.)
* ``akka.pool.running.thread.count`` -> min 0, max 3 (The three calls to ``tell``.)
* ``akka.pool.queued.task.count`` -> min 0, max 3 (The three calls to ``tell``.)
* ``akka.pool.size`` -> 15 (Depending on the default configuration for the ``ActorSystem``.)

Configuring the Akka monitor
----------------------------

The ``org.eigengo.monitor.agent.AgentConfigurationFactory`` class searches for a mandatory
configuration file at ``META-INF/monitor/agent.conf``. The configuration file must have this format:

.. code:: json
    org.eigengo.monitor.agent {
        output {
            class: "org.eigengo.monitor.output.datadog.StatsdCounterInterface"
        }

        akka {
            includeRoutees: true
            included: [
                "akka:*.com.company.project.module.TypeOfActor"
            ]
            sampling: [
                {
                    rate: 15
                    for: [ "akka:*.com.company.project.module.TypeOfActor" ]
                },
                {
                    rate: 4
                    for: [ "akka://default/user/*" ]
                }
            ]
            allowExclusions: false
            excluded: [
                "akka:*.com.company.project.module.UninterestingActor"
            ]
        }
    }


The aspect that monitors the actors in the ``ActorSystem`` can be configured to sample 1-in-n messages
to a given actor path or type.

Parameters
==========

``output.class``
    An implementation of ``org.eigengo.monitor.output.CounterInterface`` interface with a nullary
    (zero parameter) constructor.
``akka.includeRoutees``
    If ``true``, we tag routed actors with their own full actor path, as well as parent's path.
    For example, in round-robin configuration, information is tagged per actor:
    ``akka.path://default/user/bar/_a``, ``akka.path://default/user/bar/_b`` .. ``akka.path://default/user/bar/_n``
    as well in the parent: ``akka.path://default/user/bar``.
``akka.included``
    The actor classes or paths to monitor. We can match on class name or actor path. This list is
    currently treated as a conjunction -- i.e. an actor must match all filters to be included.
``akka.sampling``
    Defines the rate at which to sample messages. This is optional -- default is to sample every message.
``akka.sampling.rate``
    Integer value n - sample every n`th` message to an actor filter (starting with the first).
``akka.sampling.for``
    Associates the sampling rate with an actor filter. If an actor is included and matches this filter,
    we sample at the ``rate``. Syntax is the same as for included/excluded.
``akka.allowExclusions``
    If this is ``false``, we only include 'included' actors, if ``true``, we include all but
    ``excluded`` actors.
``akka.excluded``
    The actor classes or paths to not monitor. Same syntax and rules as included/sampling.
    See also ``allowExclusions``.

Type and path filters
---------------------

The actor path filter follows the usual Akka actor path syntax, but allows for wildcards. The expression
``akka://foo/user/bar`` includes the user actor with the name ``bar`` in the ``ActorSystem`` whose name
is ``foo``; the expression ``akka://foo/user/bar/*`` includes all children of the ``bar`` actor. The same
wildcard rules apply to the actor system name. Applying that to the expressions above, we can have
``akka://*/user/bar/*``, which matches all child actros of ``bar`` in an actor system with any name.

Similarly, the actor type filter drops the ``://`` string, includes the actor system name, and
the canonical class name of the actor. The only allowed wildcard applies to the actor system name.
Valid expressions are ``akka.foo.org.eigengo.monitor.SomeActor``, which  matches actor in class
``org.eigengo.monitor.SomeActor`` in the ``ActorSystem`` whose name is ``foo``, and
``akka.*.org.eigengo.monitor.SomeActor``, which matches actor in class ``org.eigengo.monitor.SomeActor``
in an actor system with any name.

