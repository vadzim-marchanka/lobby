package com.marchanka.lobby.impl

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.marchanka.lobby.api.Schemas.{AddTable, Table}
import com.marchanka.lobby.api._
import com.marchanka.lobby.impl.LobbyServiceSpec.{AdminRole, CallWrapper, UserRole}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.mvc.Http.HeaderNames.AUTHORIZATION


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
      client.addTable().withRole(AdminRole).invoke(AddTable(-1, 1, "table_name", 4)).map { answer =>
        answer should ===(Done)
      }
    }

    "return table after adding" in {
      for {
        _ <- client.addTable().withRole(AdminRole).invoke(AddTable(-1, 1, "table_name", 4))
        answer <- client.getTables().withRole(UserRole).invoke()
      } yield {
        answer should contain(Table(1, "table_name", 4))
      }
    }
  }

}

object LobbyServiceSpec {
  val UserRole = "user"
  val AdminRole = "admin"
  val AuthorizationType = "Basic"

  implicit class CallWrapper[Request, Response](call: ServiceCall[Request, Response]) {

    def withRole(role: String) = {
      val generatedToken = Base64.getEncoder.encodeToString(s"$role:$role".getBytes(UTF_8))
      call.handleRequestHeader(header => header.withHeader(AUTHORIZATION, s"$AuthorizationType $generatedToken"))
    }

  }

}
