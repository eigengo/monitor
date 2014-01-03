package org.eigengo.monitor.agent.play

import play.api.test.{WithApplication, PlaySpecification, FakeRequest}
import play.api.mvc.Action
import play.api.mvc.Results.Ok
import org.eigengo.monitor.{ContainsTag, TestCounterInterface}
import org.eigengo.monitor.TestCounterInterface._
import play.api.test.FakeApplication
import scala.Some

class GlobalSettingMonitoringAspectSpec extends PlaySpecification {

  val appWithRoutes = FakeApplication(withRoutes = {
    case ("GET", "/") =>
      Action {
        Ok("ok")
      }
    case ("GET", "/foo") =>
      Action {
        Ok("ok")
      }
    case ("GET", "/foo/bar") =>
      Action {
        Ok("ok")
      }
  })

  def hitUrl(url: String, r: Range): Unit = if (!r.isEmpty) {
    route(FakeRequest(GET, url))
    hitUrl(url, r.tail)
  }

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

      hitUrl("/", 1 to 3)

      val requestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount)(takeLHS)
      requestCounts.size must be equalTo(3)
    }

    "count multiple requests and apply correct tags" in new WithApplication(appWithRoutes) {
      TestCounterInterface.clear

      hitUrl("/", 1 to 3)
      hitUrl("/foo", 1 to 4)
      hitUrl("/foo/bar?a=1", 1 to 5)

      val requestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount)(takeLHS)
      requestCounts.size must be equalTo(12)

      val indexRequestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount, ContainsTag("play.request.path:/"))(takeLHS)
      indexRequestCounts.size must be equalTo(3)
      indexRequestCounts.forall(_.tags.contains("play.request.query:")) must beTrue

      val fooRequestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount, ContainsTag("play.request.path:/foo"))(takeLHS)
      fooRequestCounts.size must be equalTo(4)
      fooRequestCounts.forall(_.tags.contains("play.request.query:")) must beTrue

      val fooBarRequestCounts = TestCounterInterface.foldlByAspect(Aspects.requestCount, ContainsTag("play.request.path:/foo/bar"))(takeLHS)
      fooBarRequestCounts.size must be equalTo(5)
      fooBarRequestCounts.forall(_.tags.contains("play.request.query:a=1")) must beTrue
    }
  }
}
