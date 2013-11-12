####
Akka
####

The Akka monitoring currently logs:

* The number of messages received per actor type
* The number of actors of a given actor type
* Actor queue sizes
* Duration of an actor's response to receiving a message
* Objects published by the EventStream
* Invoked failure handling on an actor

and can be configured to sample 1-in-n messages to a given actor type


The org.eigengo.monitor.agent.AgentConfigurationFactory class searches for a mandatory configuration file at ``META-INF/monitor/agent.conf``. The configuration file must have this format::

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
    The class to be used for the output of the pointcut monitoring
``akka.includeRoutees``
    If true, we tag routed actors with their own full actor path, as well as that of the parent
``akka.included``
    The actor classes to monitor. We can match on class name or actor path. This list is currently treated as a conjunction -- i.e. an actor must match all filters to be included. This behaviour may change.
``akka.sampling``
    Defines the rate at which to sample messages. This is optional -- default is to sample every message
``akka.sampling.rate``
    Integer value 'n' - sample every 'nth' message to an actor type (starting with the first)
``akka.sampling.for``
    Associates the sampling rate with an actor filter. If an actor is included and matches this filter, we sample at the `rate`. Syntax is the same as for included/excluded.
``akka.allowExclusions``
    If this is false, we only include 'included' actors, if true, we include all but excluded actors
``akka.excluded``
    The actor classes to not monitor. Same syntax and rules as included/sampling. See also `allowExclusions`

