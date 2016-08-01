/**
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package spraybench

import akka.actor.{ActorRef, ActorSystem}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import spray.http.{ContentTypes, HttpData, HttpEntity}
import spray.routing.SimpleRoutingApp

/**
 * Minimal http server with spray to compare throughput and latencies with Akka HTTP
 */
object Server extends App with SimpleRoutingApp {




  import akka.io.IO
  import spray.can.Http

  implicit val system = ActorSystem("spray", ConfigFactory.parseString(
    """
      |akka.actor.default-dispatcher.throughput = 1000
    """.stripMargin))

  val MediumByteString = ByteString(Vector.fill(1024)(0.toByte): _*)
  val array_10x: Array[Byte] = Array(Vector.fill(10)(MediumByteString).flatten: _*)
  val array_100x: Array[Byte] = Array(Vector.fill(100)(MediumByteString).flatten: _*)
  /* val source_10x: Source[ByteString, NotUsed] = Source.repeat(MediumByteString).take(10)
  val source_100x: Source[ByteString, NotUsed] = Source.repeat(MediumByteString).take(100) */
  val tenXResponseLength = array_10x.length
  val hundredXResponseLength = array_100x.length

  // pretty much exactly the same as the akka counterpart
  startServer(interface = "localhost", port = 8080) {
    path("ping") {
      complete("PONG!")
    } ~
      path("long-response-array" / IntNumber) { n =>
        if (n == 10) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, array_10x))
        else if (n == 100) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, array_10x))
        else throw new RuntimeException(s"Not implemented for ${n}")
      }/* not really possible with spray ~
      path("long-response-stream" / IntNumber) { n =>
        if (n == 10) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, HttpData. source_100x))
        else if (n == 100) complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, source_100x))
        else throw new RuntimeException(s"Not implemented for ${n}")
      } */

  }

}
