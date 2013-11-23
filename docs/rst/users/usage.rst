#####
Usage
#####

To use the monitoring include an output module, and at least one agent module, in your project
dependencies

Configuration
=============
Create ``META-INF/monitor`` directory, containing ``agent.conf`` and ``output.conf`` files.
(More info on Akka and Statsd sections) You'll also need an ``aop.xml`` file in ``META-INF`` to
configure the load time weaving.

Dependencies
============
**Output modules**

*StatsD output for datadog*

For sbt::

    "org.eigengo.monitor" % "output-statsd" % "@version@"

For maven

.. code:: xml

    <dependency>
        <groupId>org.eigengo.monitor</groupId>
        <artifactId>output-statsd</artifactId>
        <version>@version@</version>
    </dependency>

**Monitoring Agent modules**

*Akka monitoring*

For sbt::

    "org.eigengo.monitor" % "agent-akka" % "@version@"

For maven

.. code:: xml

    <dependency>
        <groupId>org.eigengo.monitor</groupId>
        <artifactId>agent-akka</artifactId>
        <version>@version@</version>
    </dependency>