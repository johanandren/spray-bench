package spraybench

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object AkkaHttpServer extends App {

  implicit val system = ActorSystem("akka", ConfigFactory.parseString(
    """
      |akka.actor.default-dispatcher.throughput = 1000
    """.stripMargin))
  implicit val mat = ActorMaterializer()

  val MediumByteString = ByteString(Vector.fill(1024)(0.toByte): _*)
  val array_10x: Array[Byte] = Array(Vector.fill(10)(MediumByteString).flatten: _*)
  val array_100x: Array[Byte] = Array(Vector.fill(100)(MediumByteString).flatten: _*)
  val source_10x: Source[ByteString, NotUsed] = Source.repeat(MediumByteString).take(10)
  val source_100x: Source[ByteString, NotUsed] = Source.repeat(MediumByteString).take(100)
  val tenXResponseLength = array_10x.length
  val hundredXResponseLength = array_100x.length

  // pretty much exactly the same as the akka counterpart
  val routes =
    path("ping") {
      complete("PONG!")
    } ~
      path("long-response-array" / IntNumber) { n =>
        if (n == 10) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, array_10x))
        else if (n == 100) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, array_10x))
        else throw new RuntimeException(s"Not implemented for ${n}")
      } ~
      path("long-response-stream" / IntNumber) { n =>
        if (n == 10) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, source_100x))
        else if (n == 100) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, source_100x))
        else throw new RuntimeException(s"Not implemented for ${n}")
      }


  val host = "0.0.0.0"
  val port = 8080
  Http().bindAndHandle(routes, host, port)

  println(s"Running at $host:$port, ENTER to terminate")
  StdIn.readLine()
  system.terminate()
}
