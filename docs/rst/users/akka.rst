####
Akka
####

The Akka Monitor logs the following:

* For a given actor type or path, the number of messages received
* For a given actor type or path, the number of actors
* For a given actor, the queue size
* Amount of time that a given actor spends returning a response for a message
* Objects published by the ``EventStream``
* Amount of exceptions throwed by an actor

and can be configured to sample 1-in-n messages to a given actor type


The ``org.eigengo.monitor.agent.AgentConfigurationFactory`` class searches for a mandatory configuration file at ``META-INF/monitor/agent.conf``. The configuration file must have this format::

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


Parameters
==========

``output.class``
    An implementation of ``org.eigengo.monitor.output.CounterInterface`` interface with a zero parameter constructor
``akka.includeRoutees``
    If true, we tag routed actors with their own full actor path, as well as parent's path. (i.e: In Round-Robin configuration, information is tagged per actor: ``akka//:default/user/bar/_a``, ``akka//:default/user/bar/_b``.. ``akka//:default/user/bar/_n``  as well in the parent: ``akka//:default/user/bar``)
``akka.included``
    The actor classes to monitor. We can match on class name or actor path. This list is currently treated as a conjunction -- i.e. an actor must match all filters to be included.
``akka.sampling``
    Defines the rate at which to sample messages. This is optional -- default is to sample every message
``akka.sampling.rate``
    Integer value 'n' - sample every 'nth' message to an actor filter (starting with the first)
``akka.sampling.for``
    Associates the sampling rate with an actor filter. If an actor is included and matches this filter, we sample at the ``rate``. Syntax is the same as for included/excluded.
``akka.allowExclusions``
    If this is false, we only include 'included' actors, if true, we include all but excluded actors
``akka.excluded``
    The actor classes to not monitor. Same syntax and rules as included/sampling. See also ``allowExclusions``

