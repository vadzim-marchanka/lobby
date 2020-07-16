package com.marchanka.lobby.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method.{DELETE, GET, POST, PUT}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.marchanka.lobby.api.Schemas.{AddTable, Table, UpdateTable}


trait LobbyService extends Service {

  def addTable(): ServiceCall[AddTable, Done]

  def updateTable(id: Int): ServiceCall[UpdateTable, Done]

  def removeTable(id: Int): ServiceCall[NotUsed, Done]

  def getTables(): ServiceCall[NotUsed, Vector[Table]]

  def getPing(seq: String): ServiceCall[NotUsed, String]

  override final def descriptor: Descriptor = {
    import Service._
    named("lobby")
      .withCalls(
        restCall(POST, apiUrl("admin/tables"), addTable _),
        restCall(PUT, apiUrl("admin/tables/:id"), updateTable _),
        restCall(DELETE, apiUrl("admin/tables/:id"), removeTable _),
        restCall(GET, apiUrl("user/tables"), getTables _),
        restCall(GET, apiUrl("user/ping/:seq"), getPing _)
      )
      .withAutoAcl(true)
  }

  private def apiUrl(endpoint: String) = s"/lobby_api/$endpoint"

}

object LobbyService {

}
