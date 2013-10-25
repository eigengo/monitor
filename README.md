#Monitoring of stuff

Include one ``*-agent``, create ``/META-INF/aop.xml`` that liststs the aspects to be weaved in. Include
one of the ``*-output`` dependencies so that the aspects know how to send the metrics out.

No Maven dependecies between the agent and ouput. We may have ``output-api`` module, which defines some classes 
or interfaces that all output modules must implement. The agents and the outputs depend on the api.

##Example project

###Dependencies
Add ``org.eigengo.monitor:akka-agent:0.1``, ``org.eigengo.monitor:statsd-output:0.1``. 

###Configuration
In your project (not the monitor), create:

```
/META-INF/aop.xml <- lists the aspects from akka-agent to be weaved in
/META-INF/monitor/agent.conf <- Typesafe config-style settings for the agent
/META-INF/monitor/statsd.conf <- Tyepsafe config-style settings for the output
```

Know the class name that will be used as output.

#Monitoring other stuff
In Play, use ``org.eigengo.montor:play-agent:0.1`` with the desired output module.

