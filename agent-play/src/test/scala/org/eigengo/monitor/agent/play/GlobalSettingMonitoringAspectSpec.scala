package org.eigengo.monitor.agent.play

import play.api.test.{WithApplication, PlaySpecification, FakeRequest}
import play.api.mvc.Action
import play.api.mvc.Results.Ok
import org.eigengo.monitor.TestCounterInterface
import org.eigengo.monitor.TestCounterInterface._
import play.api.test.FakeApplication
import scala.Some

class GlobalSettingMonitoringAspectSpec extends PlaySpecification {

  val appWithRoutes = FakeApplication(withRoutes = {
    case ("GET", "/") =>
      Action {
        Ok("ok")
      }
  })

  "The GlobalSettingMonitoringAspect" should {

    "count a single request" in new WithApplication(appWithRoutes) {
      TestCounterInterface.clear
      val Some(result) = route(FakeRequest(GET, "/"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/plain")
      charset(result) must beSome("utf-8")
      contentAsString(result) must contain("ok")
      val requestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount)(takeLHS)
      requestCounts.size must be equalTo(1)
    }

    "count multiple requests" in new WithApplication(appWithRoutes) {
      TestCounterInterface.clear

      def hitIndex(r: Range): Unit = if (!r.isEmpty) {
        route(FakeRequest(GET, "/"))
        hitIndex(r.tail)
      }
      hitIndex(1 to 3)

      val requestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount)(takeLHS)
      requestCounts.size must be equalTo(3)
    }
  }
}
