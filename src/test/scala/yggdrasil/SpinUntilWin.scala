package yggdrasil

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SpinUntilWin extends Simulation {

  val httpProtocol = http
    .baseURL("https://pff.yggdrasilgaming.com")
    //    .proxy(Proxy("localhost", 8888).httpsPort(8888))
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko")

  val headers_1 = Map(
    "Accept" -> "*/*",
    "Accept-Encoding" -> "gzip, deflate",
    "Accept-Language" -> "en-US,en;q=0.7,ru;q=0.3",
    "Connection" -> "Keep-Alive",
    "DNT" -> "1",
    "Origin" -> "https://staticpff.yggdrasilgaming.com")

  val scn = scenario("SpinUntilWin")
    .exec(http("request_authenticate")
      .get("/game.web/service?fn=authenticate&org=Demo&lang=en&gameid=7316&channel=mobile&currency=EUR")
      .headers(headers_1)
      .check(status.is(200))
      .check(jsonPath("$..sessid").saveAs("sessid"))
    )

    .asLongAs(session => session("accWa").asOption[String].forall(accWa => accWa.toDouble == 0.00)) {
      exec(http("request_bet")
        .get("/game.web/service?fn=play&currency=EUR&gameid=7316&sessid=${sessid}&log=DefB%2C0.05%2FBetChd%2C0.05%2FCurrChd%2Ctrue%2FBPanl%2C0.05%2F&amount=1.25&lines=1111111111111111111111111&coin=0.05")
        .headers(headers_1)
        .check(status.is(200))
        .check(jsonPath("$..accWa").saveAs("accWa"))
        .check(jsonPath("$..wagerid").saveAs("wagerid"))
      )
    }

    .exec(http("request_won_ammount")
      .get("/game.web/service?fn=play&currency=EUR&gameid=7316&sessid=${sessid}&log=&amount=0&wagerid=${wagerid}&betid=1&step=2&cmd=C")
      .headers(headers_1)
      .check(status.is(200))
      .check(jsonPath("$..wonamount").not("0.00"))
    )

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)

}
