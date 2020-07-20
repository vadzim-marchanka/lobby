package com.marchanka.lobby.impl

import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.marchanka.lobby.api.Schemas.Table
import com.marchanka.lobby.api.{LobbyService, Schemas}
import com.marchanka.lobby.impl.LobbyServiceImpl.{ResultFutureWrapper, TablesId}
import com.marchanka.lobby.impl.common.AuthorizationChecker
import com.marchanka.lobby.impl.common.AuthorizationChecker.{AdminRole, UserRole}
import com.marchanka.lobby.impl.tables.TablesCommands._
import com.marchanka.lobby.impl.tables.TablesPersistence.TablesState

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class LobbyServiceImpl(
                        clusterSharding: ClusterSharding
                      )(implicit ec: ExecutionContext)
  extends LobbyService with AuthorizationChecker {

  private def tablesEntityRef(): EntityRef[TablesCommand] =
    clusterSharding.entityRefFor(TablesState.typeKey, TablesId)

  implicit val timeout = Timeout(10.seconds)

  override def addTable(): ServiceCall[Schemas.AddTable, Done] =
    withRoleAuthorization(AdminRole)(rq =>
      tablesEntityRef()
        .ask[OperationResult](replyTo => AddTable(rq.afterId, rq.id, rq.name, rq.participants, replyTo))
        .handleResult()
    )

  override def updateTable(id: Int): ServiceCall[Schemas.UpdateTable, Done] =
    withRoleAuthorization(AdminRole)(rq =>
      tablesEntityRef()
        .ask[OperationResult](replyTo => UpdateTable(id, rq.name, rq.participants, replyTo))
        .handleResult()
    )

  override def removeTable(id: Int): ServiceCall[NotUsed, Done] =
    withRoleAuthorization(AdminRole)(_ =>
      tablesEntityRef()
        .ask[OperationResult](replyTo => RemoveTable(id, replyTo))
        .handleResult()
    )

  override def getTables(): ServiceCall[NotUsed, Vector[Schemas.Table]] =
    withRoleAuthorization(AdminRole, UserRole) { _ =>
      tablesEntityRef()
        .ask[Tables](replyTo => GetTables(replyTo))
        .map(_.tables.map(t => Table(t.id, t.name, t.participants)))
    }

  override def getPing(seq: String): ServiceCall[NotUsed, String] =
    withRoleAuthorization(AdminRole, UserRole) { _ =>
      Future.successful(seq)
    }

}

object LobbyServiceImpl {
  val TablesId = "1"

  implicit class ResultFutureWrapper(futureResult: Future[OperationResult]) {
    def handleResult()(implicit ec: ExecutionContext): Future[Done] = {
      futureResult.map {
        case SuccessfulOperation => Done
        case FailedOperationWithBadRequest(message) => throw BadRequest(message)
        case FailedOperationWithNotFound => throw NotFound("The given entity does not exist")
      }
    }
  }

}
