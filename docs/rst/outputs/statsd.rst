######
Statsd
######

The ``org.eigengo.monitor.output.statsd.StatsdCounterInterface`` class searches for a mandatory configuration
file at ``META-INF/monitor/output.conf``. This configuration file must have this format.::

    org.eigengo.monitor.output.statsd {
        prefix: ""
        remoteAddress: "localhost"
        remotePort: 8125
        refresh: 5
        initialDelay: 5
        constantTags: []
    }

Parameters
==========

``prefix``
    A prefix that will be applied to every key.
``remoteAddress``
    Host name for DataDog agent
``remotePort``
    Port number for DataDog agent
``refresh``
    Amount of seconds for resending saved information to DataDog agent (In order to show proper graphics DataDog
    needs a constant information stream)
``initialDelay``
    Amount of seconds to execute the first refresh since the application start
``constantTags``
    Constant tags for every key (Check http://docs.datadoghq.com/guides/dogstatsd/#tags )

Show detailed graphs in DataDog Console
=======================================

By default DataDog will create a "Custom Metrics - akka" dashboard with general information about the Akka
system. More detailed information is available but some configuration is needed on DataDog console.

DataDog support a JSON formatted configuration to modify the graphs
(More info on http://docs.datadoghq.com/graphing/ )

For our example application, you could use this configuration to show every actor count in a separate
line

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


Also is possible to edit this configuration in a graphic way using the "Edit" tab

.. raw:: latex

    \newpage