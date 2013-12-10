###############
Getting started
###############

To use the monitoring, include an output module, and at least one agent module, in your project's
dependencies. Once you have included the agent and output modules, you will need to provide their
configuration.

Minimal application
===================

Create the ``META-INF/monitor`` directory, containing ``agent.conf`` and ``output.conf`` files.
The contents of the files depend on the agent and output module you have decided to use; in principle,
the ``agent.conf`` file contains configuration for the agent, and the ``output.conf`` contains configuration
for the output module. The ``agent.conf`` also specifies the name of the class that the agent will use
to submit the collected statistics. In addition to the two ``.conf`` files, you  will also need the
``aop.xml`` file in the ``META-INF`` directory to configure AspectJ's load time weaver.

For example, if you decide to use the statsd output module to monitor an Akka application, you will
need to add the ``output-statsd`` and ``agent-akka`` dependencies to your project.

In sbt, one writes::

    "org.eigengo.monitor" % "output-statsd" % "@version@",
    "org.eigengo.monitor" % "agent-akka" % "@version@"

or, in Maven, one adds:

.. code:: xml

    <dependency>
        <groupId>org.eigengo.monitor</groupId>
        <artifactId>output-statsd</artifactId>
        <version>@version@</version>
    </dependency>
    <dependency>
        <groupId>org.eigengo.monitor</groupId>
        <artifactId>agent-akka</artifactId>
        <version>@version@</version>
    </dependency>

Once the dependencies are resolved, you must create the configuration files for the agent and the output.
Starting the tour with the ``META-INF/monitor/agent.conf``, we have::

    org.eigengo.monitor.agent {
        output {
            class: "org.eigengo.monitor.output.statsd.StatsdCounterInterface"
        }

    }

Following on with the ``META-INF/monitor/output.conf``::

    org.eigengo.monitor.output.statsd {
        prefix: ""
        remoteAddress: "localhost"
        remotePort: 8125
        refresh: 5
        initialDelay: 5
        constantTags: []
    }

And finishing with the ``META-INF/aop.xml``

.. code:: xml

    <aspectj>

        <aspects>
            <aspect name="org.eigengo.monitor.agent.akka.ActorCellMonitoringAspect"/>
            <aspect name="org.eigengo.monitor.agent.akka.DispatcherMonitoringAspect"/>
        </aspects>

        <weaver options="-verbose -showWeaveInfo">
            <include within="akka.actor.*"/>
            <include within="akka.dispatch.*"/>
            <include within="scala.concurrent.*"/>
            <include within="java.util.concurrent.*"/>
        </weaver>

    </aspectj>

Once you have the dependencies and the configuration files, you can create a simple ``ActorSystem``,
add an ``Actor`` and observe the statistics Monitor collects.

.. code:: scala

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

The very last thing you will need is to specify the ``-javaagent:`` parameter to the JVM when starting
the application. The agent is the AspectJ's load-time weaver, which is going to inject the monitoring
code into the bytecode of your application.

Typically, you will need to include the ``-javaagent:/.../aspectjweaver-1.7.3.jar`` JVM parameter. If you
have configured everything correctly, and you start your application, you should see output similar
to::

    [AppClassLoader@3432a325] info register classloader Launcher$AppClassLoader@123
    [AppClassLoader@3432a325] info using configuration /.../META-INF/aop.xml
    [AppClassLoader@3432a325] info using configuration /.../META-INF/aop-ajc.xml
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.ActorCellMonitoringAspect
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.DispatcherMonitoringAspect
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.DispatcherMonitoringAspect
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.ActorCellMonitoringAspect
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.Pointcuts
    [AppClassLoader@3432a325] info register aspect
        org.eigengo.monitor.agent.akka.AbstractMonitoringAspect
    [AppClassLoader@3432a325] weaveinfo Join point
        'method-execution(akka.actor.ActorRef ...)'
    ...
    [AppClassLoader@3432a325] weaveinfo Join point
        'method-execution(void akka.dispatch.BalancingDispatcher...)'

Your application will then publish the statistics to the statsd agent running on ``localhost:8125``;
it will include all actors and all messages.

This is exactly the approach that the ``example-akka`` module uses; take a look through its source
code, and then execute it by running ``sbt run``, and issuing several ``go`` commands.

.. code:: bash

    monitor$ sbt run
    ...
    [info] Running org.eigengo.monitor.example.akka.Main
    go

    ...
    Counting down... Now 8
    Bar done.
    Counting down... Now 7
    Counting down... Now 6
    Counting down... Now 5
    Counting down... Now 4
    Counting down... Now 3
    Counting down... Now 2
    Counting down... Now 1
    Foo done.

The ``go`` command is the text you type in on the standard input; the application will then produce the
``Counting down...``, ``Bar done.`` and ``Foo done.`` output.

Overview of modules
===================

Monitor is split into multiple modules; the motivation is to keep maintain very loose coupling. Such
structure lets you deploy the monitor into various applications, be it :ref:`Akka <agent_akka>`,
:ref:`Play <agent_play>`, or :ref:`Spray <agent_spray>`. The agent modules rely on the implementations
of some output modules. Monitor includes support for the :ref:`Statsd <output_statsd>` and
:ref:`Codahale Metrics <output_codahalemetrics>`.

Depending on the application you are developing, you will need to include the appropriate output module.
Notably, if the application you are monitoring needs *multiple* output modules, you may include as many
output modules as you require. So, if you have a very complex---though we say offer no comment whether this is
a good idea---include multiple agent modules, and modify the ``aop.xml`` file appropriately to include
the aspects you are using.

By convention, all agents load their configuration from the file ``META-INF/monitor/agent.conf``, but
from a specific section. Therefore, if your application includes multiple modules, you will need to
merge the specific configuration settings.

Agent modules
-------------

The monitor project includes agents for :ref:`Akka <agent_akka>`, :ref:`Play <agent_play>`, and
:ref:`Spray <agent_spray>`.

The :ref:`Akka agent module <agent_akka>` records the details of (local) actor systems: the performance &
error rates of the actors, as well as the performance & health of the dispatchers and thread pools. To
use the Akka agent, include the dependency on::

    "org.eigengo.monitor" % "agent-akka" % "@version@"

The Play agent collects the statistics about the Play application. TBC.

The Spray agent collects the statistics about the Spray HTTP(s) layer. TBC.

Output modules
--------------

The output modules allow the statistics collected by the agents above to reach the ultimate metrics
recording / monitoring target. We support output modules that record the data in :ref:`Statsd <output_statsd>`,
and :ref:`Codahale Metrics <output_codahalemetrics>`.

The :ref:`Statsd output module <output_statsd>` uses the `Datadog <http://datadoghq.com>`_ extensions,
and thus does not support plain statsd servers. To use the Statsd output module, include the dependency
on::

    "org.eigengo.monitor" % "output-statsd" % "@version@"

The :ref:`Codahale Metrics output module <output_codahalemetrics>` supports the output of recorded data to a
`Metric Registry <http://metrics.codahale.com/manual/core/#metric-registries>`_. To include the
Codahale Metrics output module, include the dependency on::

    "org.eigengo.monitor" % "output-codahalemetrics" % "@version@"

