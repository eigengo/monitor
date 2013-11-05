################
Building Monitor
################

To build *Monitor*, you will need the usual `Scala <http://scala-lang.org>`_ toolset:

* JDK 1.6 or 1.7 (we target 1.6)
* Sbt (we recommend `sbt-extras <https://github.com/paulp/sbt-extras>`_.)
* Sphinx

To get started, clone the repository from `https://github.com/eigengo/monitor <https://github.com/eigengo/monitor>`_. Once cloned, change into that directory and run ``sbt``. From the Sbt prompt, you can then execute the tasks to build Monitor.

* ``clean`` removes all generated files,
* ``test`` builds the sources, and runs all required tests,
* ``scalacheck`` to verify that the code satisfies our style guidelines,
* ``sphinx:generatePdf``, ``sphinx:generateEpub`` or ``sphinx:generateHtml`` to generate the latest documentation

IDE support
===========

The `Scala IDE <http://scala-ide.org>`_ supports Sbt-based project "out of the box". For `IntelliJ IDEA <http://www.jetbrains.com/idea>`_, you will need to generate the project files by running ``sbt gen-idea``. 

Once open, you can perform the tasks 

Troubleshooting
===============

Always 