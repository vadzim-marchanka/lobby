package com.marchanka.lobby.impl

import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.marchanka.lobby.api.Schemas.Table
import com.marchanka.lobby.api.{LobbyService, Schemas}
import com.marchanka.lobby.impl.LobbyServiceImpl.TablesId
import com.marchanka.lobby.impl.tables.TablesCommands.{AddTable, GetTables, RemoveTable, Tables, TablesCommand, UpdateTable}
import com.marchanka.lobby.impl.tables.TablesPersistence.TablesState

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the lobbyService.
  */
class LobbyServiceImpl(
                        clusterSharding: ClusterSharding
                      )(implicit ec: ExecutionContext)
  extends LobbyService {

  private def tablesEntityRef(): EntityRef[TablesCommand] =
    clusterSharding.entityRefFor(TablesState.typeKey, TablesId)

  implicit val timeout = Timeout(5.seconds)


  override def addTable(): ServiceCall[Schemas.AddTable, Done] = { rq =>
    tablesEntityRef()
      .tell(AddTable(rq.id, rq.name, rq.participants))
    Future.successful(Done)
  }

  override def getTables(): ServiceCall[NotUsed, Vector[Schemas.Table]] = { _ =>
    tablesEntityRef()
      .ask[Tables](replyTo => GetTables(replyTo))
      .map(_.tables.map(t => Table(t.id, t.name, t.participants)))
  }

  override def updateTable(id: Int): ServiceCall[Schemas.UpdateTable, Done] = { rq =>
    tablesEntityRef()
      .tell(UpdateTable(id, rq.name, rq.participants))
    Future.successful(Done)
  }

  override def removeTable(id: Int): ServiceCall[NotUsed, Done] = { _ =>
    tablesEntityRef().tell(RemoveTable(id))
    Future.successful(Done)
  }

  override def getPing(seq: String): ServiceCall[NotUsed, String] = { _ =>
    Future.successful(seq)
  }
}

object LobbyServiceImpl {
  val TablesId = "1"
}
