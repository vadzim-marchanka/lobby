package com.marchanka.lobby.impl

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.marchanka.lobby.api.Schemas.{AddTable, Table, UpdateTable}
import com.marchanka.lobby.api._
import com.marchanka.lobby.impl.LobbyServiceSpec.{AdminRole, CallWrapper, UserRole}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.mvc.Http.HeaderNames.AUTHORIZATION

import scala.util.Random


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
      val id = generateTableId()
      client.addTable().withRole(AdminRole).invoke(AddTable(-1, id, "table_name", 4)).map { answer =>
        answer should ===(Done)
      }
    }

    "return table after adding" in {
      val id = generateTableId()
      for {
        _ <- client.addTable().withRole(AdminRole).invoke(AddTable(-1, id, "table_name", 4))
        answer <- client.getTables().withRole(UserRole).invoke()
      } yield {
        answer should contain(Table(id, "table_name", 4))
      }
    }

    "update table" in {
      val id = generateTableId()
      for {
        _ <- client.addTable().withRole(AdminRole).invoke(AddTable(-1, id, "table_name", 4))
        _ <- client.updateTable(id).withRole(AdminRole).invoke(UpdateTable("updated table_name", 10))
        answer <- client.getTables().withRole(UserRole).invoke()
      } yield {
        answer should contain(Table(id, "updated table_name", 10))
      }
    }

    "delete table" in {
      val id = generateTableId()
      for {
        _ <- client.addTable().withRole(AdminRole).invoke(AddTable(-1, id, "table_name", 4))
        _ <- client.removeTable(id).withRole(AdminRole).invoke()
        answer <- client.getTables().withRole(UserRole).invoke()
      } yield {
        answer.find(_.id == id) should ===(None)
      }
    }

  }

  private def generateTableId() = Random.nextInt(100000)

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
