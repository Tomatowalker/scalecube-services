import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._


class RequestStreamTest extends Simulation {
  // Base test parameters
  val host = System.getProperty("host", "127.0.0.1")
  val port = Integer.getInteger("port", 8080)
  val users = Integer.getInteger("users", 5000)
  val frequencyMillis = Integer.getInteger("frequencyMillis", 1000)
  val duration = Integer.getInteger("duration", 120)
  // Derived test parameters: 1/10 - rampup, 8/10 - load, 1/10 - rampdown
  val rampUpDuration: Integer = duration.toInt / 10
  val scenarioDuration: Integer = 2 * duration.toInt / 10 * 8
  val echoParam = """echoName"""

  // Scenario parameters
  val url = s"ws://$host:$port/greeting/manyStream"
  val echoRequest = s"""{"name":"$echoParam", "frequency":$frequencyMillis}"""
  val httpConfig = http.baseURL(s"http://$host:$port")

  // User scenario is a chain of requests and pauses
  val usersScn =
    scenario("RequestStreamTest")
      .exec(ws("socket").open(url))
      .pause(2 seconds)
      .exec(ws("Many Request")
        .sendText(echoRequest)
        .check(wsAwait.within(scenarioDuration seconds).until(scenarioDuration - 100).regex( s"""${echoParam}""")))
      .pause(1 second)
      .exec(ws("Close").close)

  // Start user scenarios
  setUp(usersScn.inject(
    rampUsers(users) over (rampUpDuration seconds))
  ).protocols(httpConfig)
}
