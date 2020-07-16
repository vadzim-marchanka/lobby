package com.marchanka.lobby.impl

import akka.Done
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.marchanka.lobby.api.Schemas.{AddTable, Table}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.marchanka.lobby.api._

class LobbyServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new LobbyApplication(ctx) with LocalServiceLocator
  }

  val client: LobbyService = server.serviceClient.implement[LobbyService]

  override protected def afterAll(): Unit = server.stop()

  "lobby service" should {

    "add table" in {
      client.addTable().invoke(AddTable(1, "table_name", 4)).map { answer =>
        answer should === (Done)
      }
    }

    "return table after adding" in {
      for {
        _ <- client.addTable().invoke(AddTable(1, "table_name", 4))
        answer <- client.getTables().invoke()
      } yield {
        answer should ===(Vector(Table(1, "table_name", 4)))
      }
    }
  }
}
