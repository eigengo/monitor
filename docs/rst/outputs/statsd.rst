.. _output_statsd:

####################
Statsd output module
####################

The statsd output module sends the collected statistics to a statsd server. Statsd is a very low overhead
protocol, and is implemented by various vendors. Notably, we are using the `Datadog <http://datadoghq.com>`_
extensions to the statsd protocol.

Dependencies
============
To include this output module, add the ``org.eigengo.monitor % output-statsd % @version@`` to your
project's dependencies. The Statsd output module is built using Scala 2.10.3, Akka 2.2.0, and
the `Java Dogstatsd library <https://github.com/indeedeng/java-dogstatsd-client>`_ in version 2.0.7.
It is compatible with any 2.10 version of Scala.

The agent has no further dependencies; it can be added to any Scala or Java-based Akka project.

Exposed CounterInterfaces
=========================
The module exposes two implementations of the ``CounterInterface``:

* ``org.eigengo.monitor.output.statsd.StatsdCounterInterface`` uses the Dogstatsd client
* ``org.eigengo.monitor.output.statsd.AkkaIOStatsdCounterInterface`` uses the Akka IO implementation
  of the statsd protocol

Configuration
=============

Both implementations use the same configuration details, loaded from the ``META-INF/monitor/output.conf``.
The module loads the ``org.eigengo.monitor.output.statsd`` in that configuration file. An example
configuration file is:

.. code:: json

    org.eigengo.monitor.output.statsd {
        prefix: ""
        remoteAddress: "localhost"
        remotePort: 8125
        refresh: 5
        initialDelay: 5
        constantTags: []
    }

Parameters
----------

.. tabularcolumns:: |l|l|p{11cm}|

=================  ========  ===========================================================================
Key                Type      Description
=================  ========  ===========================================================================
``prefix``         string    A prefix that will be applied to every key
``remoteAddress``  string    Host name or IP address for Datadog agent
``remotePort``     number    Port number for Datadog agent
``refresh``        number    Number of seconds for resending saved information to Datadog agent;
                             in order to show proper graphics Datadog needs a constant information
                             stream
``initialDelay``   number    Number of seconds to execute the first refresh since the application start
``constantTags``   [string]  Constant tags for every key;
                             viz `Datadog tags <http://docs.datadoghq.com/guides/dogstatsd/#tags>`_
=================  ========  ===========================================================================

Show detailed graphs in Datadog Console
=======================================

By default Datadog will create a "Custom Metrics - Akka" dashboard with general information about the Akka
system. More detailed information is available but some configuration is needed on Datadog console.

Datadog support a JSON formatted configuration to modify the graphs. For more information review
`Datadog Docs - Graphing <http://docs.datadoghq.com/graphing/>`_.

For our example application, you could use this configuration to show every actor count in a separate
line.

.. code:: json

    {
      "viz": "timeseries",
      "requests": [
        {
          "q": "sum:akka.actor.count{org.eigengo.monitor.example.akka.main.baractor}"
        },
        {
          "q": "sum:akka.actor.count{org.eigengo.monitor.example.akka.main.fooactor}"
        }
      ],
      "events": []
    }

TBC.

.. raw:: latex

    \newpage