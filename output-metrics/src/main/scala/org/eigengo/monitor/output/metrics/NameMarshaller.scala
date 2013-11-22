/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.output.metrics


/**
 * Turns the ``MetricsStatistic`` and some ``prefix`` into a name
 */
trait NameMarshaller {
  def prefix: String

  def buildName(aspect: String, tags: Seq[String]): String
}

/**
 *
 * @param prefix the constant prefix for all messages. Must be empty or end with ``.``
 */
class DefaultNameMarshaller(val prefix: String) extends NameMarshaller {

  require(prefix.isEmpty || prefix.endsWith("."), "Prefix must be empty or end with '.'")

  override def buildName(aspect: String, tags: Seq[String]): String = {

    def cleanPath: String = (userPathString(tags.head.replace("akka.path:/", "")))

    val name = aspect match {
      case "akka.actor.count" =>
        // This is the actor count
        "actor-count"
      case "akka.queue.size" =>
        // This is the queue size
        s"$cleanPath.queue-size"
      case _ => // These are all actor specific
        s"$cleanPath.${aspect.replace("akka.actor.", "").replace("$", "").toLowerCase}"
    }

    s"$prefix$name"
  }

  /**
   * Convert 'user' actors such as 'akka://server/user/myactor' to 'myactor'.
   * Convert 'system' actors such as 'akka://server/system/'
   * @param str
   * @return
   */
  def userPathString(str: String): String = {
    (str match {
      case s if s.startsWith("server/user/") => s.stripPrefix("server/user/")
      case s if s.startsWith("server/system/") => s.replace("server/system/", "internal.")
      case s => s.replace("server/", "internal.")
    }).replace('/', '.').toLowerCase
  }
}

object NameMarshallerFactory {
  /**
   * Create an instance of a ``NameMarshaller`` given the passed class
   * @param clazz the fully qualified name of the class to instantiate
   * @return an instance of ``NameMarshaller``
   */
  private[metrics] def getNameMarshaller(clazz: String, prefix: String): NameMarshaller = {
    try {
      val constructor = Class.forName(clazz).getConstructors()(0)
      constructor.newInstance(prefix).asInstanceOf[NameMarshaller]
    }
    catch {
      case e: ReflectiveOperationException => new DefaultNameMarshaller(prefix)
      case e: ClassCastException => new DefaultNameMarshaller(prefix)
    }
  }
}
