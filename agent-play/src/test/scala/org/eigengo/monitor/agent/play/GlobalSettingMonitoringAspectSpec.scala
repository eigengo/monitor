package org.eigengo.monitor.agent.play

import play.api.test.{FakeApplication, WithApplication, PlaySpecification, FakeRequest}
import play.api.mvc.Action
import play.api.mvc.Results.Ok

class GlobalSettingMonitoringAspectSpec extends PlaySpecification {

  val appWithRoutes = FakeApplication(withRoutes = {
    case ("GET", "/") =>
      Action {
        Ok("ok")
      }
  })

  "respond to the index Action" in new WithApplication(appWithRoutes) {
    val foo = route(FakeRequest(GET, "/"))
    val Some(result) = route(FakeRequest(GET, "/"))

    status(result) must equalTo(OK)
    contentType(result) must beSome("text/plain")
    charset(result) must beSome("utf-8")
    contentAsString(result) must contain("ok")
  }

}
