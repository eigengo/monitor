.. _output_codahalemetrics:

################
Codahale Metrics
################

The Codahale Metrics output module sends the collected statistics to a provided Codahale metrics registry. Codahale
Metrics is a Java metrics library that is used by many services. More information for `Codahale <http://metrics.codahale.com>`_
can be obtained here.

Dependencies
============
To include this output module, add the ``org.eigengo.monitor % output-codahalemetrics % @version@`` to your
project's dependencies. The Codahale Metrics output module is built using Scala 2.10.3, Akka 2.2.0, and
the `Codahale Metrics library <https://github.com/codahale/metrics/tree/v3.0.1>`_ in version 3.0.1.
It is compatible with any 2.10 version of Scala.

The agent has no further dependencies; it can be added to any Scala or Java-based Akka project.

Exposed CounterInterfaces
=========================
The module exposes two implementations of the ``CounterInterface``:

* ``org.eigengo.monitor.output.codahalemetrics.MetricsCounterInterface`` writes statistics directly to a provided
  registry.

* ``org.eigengo.monitor.output.codahalemetrics.AkkaMetricsCounterInterface`` writes statistics to an Akka actor where
  it is written to a provided registry.

Configuration
=============

Both implementations use the same configuration details, loaded from the ``META-INF/monitor/output.conf``.
The module loads the ``org.eigengo.monitor.output.codahalemetrics`` in that configuration file. An example
configuration file is:

.. code:: json

    org.eigengo.monitor.output.codahalemetrics {
        prefix: ""
        registry-class: "org.eigengo.monitor.output.codahalemetrics.DefaultRegistryProvider"
        naming-class: "org.eigengo.monitor.output.codahalemetrics.DefaultNameMarshaller"
    }

Parameters
----------

.. tabularcolumns:: |l|l|p{11cm}|

=================   ========  ===========================================================================
Key                 Type      Description
=================   ========  ===========================================================================
prefix              string    A prefix that will be applied to every key
registry-class      string    The fully qualified name for a class that extends RegistryProvider which provides
                              an instance of com.codahale.metrics.MetricRegistry. The registry can be shared
                              with other application statistics or just the ones added through this library. By
                              default, one can use DefaultRegistryProvider in lieu of creating one.
naming-class        string    The fully qualified name for a class that extends NameMarshaller which provides
                              a naming scheme for the statistics. By default, one can use DefaultNameMarshaller
                              in lieu of creating one.
=================   ========  ===========================================================================

Providing a Codahale Registry
=======================================
xyz

Providing a Name Marshaller
=======================================
xyz

TBC.

.. raw:: latex

    \newpage