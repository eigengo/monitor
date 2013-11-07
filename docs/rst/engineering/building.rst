################
Building Monitor
################

To build *Monitor*, you will need the usual `Scala <http://scala-lang.org>`_ toolset:

JDK 1.6 or 1.7 (we target 1.6)
==============================

We build Monitor using Oracle's JDK 1.7 and OpenJDK 1.7. Select your favourite JDK, download & install suitable distribution for your platform.

Sbt
===

We use Sbt 0.13 to build Monitor. Follow the `plain-vanilla Sbt setup <http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html>`_, or use our vavourite `sbt-extras <https://github.com/paulp/sbt-extras>`_.

Sphinx
======

To generate the documentation, we use Sphinx (with pygments and TeX). Once you have Python, you need to get Sphinx ``easy_install sphinx``.

Building
========

To get started, clone the repository from `https://github.com/eigengo/monitor <https://github.com/eigengo/monitor>`_. Once cloned, change into that directory and run ``sbt``. From the Sbt prompt, you can then execute the tasks to build Monitor.

* ``clean`` removes all generated files,
* ``test`` builds the sources, and runs all required tests,
* ``scalacheck`` to verify that the code satisfies our style guidelines,
* ``sphinx:generatePdf``, ``sphinx:generateEpub`` or ``sphinx:generateHtml`` to generate the latest documentation

IDE support
===========

The `Scala IDE <http://scala-ide.org>`_ supports Sbt-based project "out of the box". For `IntelliJ IDEA <http://www.jetbrains.com/idea>`_, you will need to generate the project files by running ``sbt gen-idea``. 

Troubleshooting
===============

If you have a suggestion for a new feature, or if you have found a bug in the code, create an issue at `https://github.com/eigengo/monitor/issues <https://github.com/eigengo/monitor/issues>`_. 

As always, things may be too complex to squeeze into a simple GitHub issue. If you get stuck, the core team will assist you over e-mail at { ``jan.machacek``, ``anirvan.chakraborty`` } at ``gmail.com``, { ``hughs``, ``alexl``, ``cornelf`` } at ``cakesolutions.net``. 
