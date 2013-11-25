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
package org.eigengo.monitor.output.codahalemetrics

import org.specs2.mutable.Specification

class NameMarshallerSpec  extends Specification {

  "The name marshaller functionality" should {

    "allow for the creation of a valid NameMarshaller" in {
      val marshaller = NameMarshallerFactory.getNameMarshaller("org.eigengo.monitor.output.codahalemetrics.TestNameMarshaller", "prefix.")
      marshaller must beAnInstanceOf[TestNameMarshaller]
    }

    "create a default NameMarshaller when an invalid class is given" in {
      val marshaller = NameMarshallerFactory.getNameMarshaller("myclass", "prefix.")
      marshaller must beAnInstanceOf[DefaultNameMarshaller]
    }

    "build an appropriate name when given an aspect and tags" in {
      val prefix = "prefix."
      val marshaller = new DefaultNameMarshaller(prefix)
      val name = marshaller.buildName("akka.queue.size", Seq("akka.path:/server/user/my-actor", "akka.type:com.foo.MyActor"))
      name must be equalTo s"${prefix}server.user.my-actor.queue-size"

      val internalName = marshaller.buildName("akka.queue.size", Seq("akka.path:/server/system/some-akka-actor", "akka.type:com.foo.SomeAkkaActor"))
      internalName must be equalTo s"${prefix}server.system.some-akka-actor.queue-size"
    }
  }
}

class TestNameMarshaller(val prefix: String) extends NameMarshaller {

  override def buildName(aspect: String, tags: Seq[String]): String = {
    ""
  }
}

