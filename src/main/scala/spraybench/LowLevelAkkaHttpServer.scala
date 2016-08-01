package spraybench

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import HttpMethods.GET
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.{Segment, Slash}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object LowLevelAkkaHttpServer extends App {

  implicit val system = ActorSystem("lowlevel-akka-http",
    ConfigFactory.parseString(
      """
        |akka.actor.default-dispatcher.throughput = 1000
      """.stripMargin))

  implicit val mat = ActorMaterializer()

  Http().bindAndHandleSync({
    case HttpRequest(GET, Uri(_, _, Slash(Segment("ping", Path.Empty)), _, _), _, _, _) =>
      HttpResponse(entity = "PONG!")
    case _ =>
      HttpResponse(status = StatusCodes.NotFound)
  },
  "0.0.0.0",
    8080
  )

  println("ENTER to terminate")
  StdIn.readLine()
  system.terminate()

}
