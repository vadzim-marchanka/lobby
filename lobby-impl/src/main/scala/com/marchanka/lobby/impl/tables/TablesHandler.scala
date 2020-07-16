package com.marchanka.lobby.impl.tables

import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.marchanka.lobby.impl.tables.TablesCommands._
import com.marchanka.lobby.impl.tables.TablesPersistence.{TableAdded, TableRemoved, TableUpdated, TablesEvent, TablesState}

trait TablesHandler {
  this: TablesState =>

  def applyCommand(cmd: TablesCommand): ReplyEffect[TablesEvent, TablesState] =
    cmd match {
      case AddTable(id, name, participants) =>
        Effect.persist(TableAdded(id, name, participants)).thenNoReply()
      case UpdateTable(id, name, participants) =>
        Effect.persist(TableUpdated(id, name, participants)).thenNoReply()
      case RemoveTable(id) =>
        Effect.persist(TableRemoved(id)).thenNoReply()
      case GetTables(actorRef) =>
        Effect.reply(actorRef)(Tables(this.tables.map(t => TablesCommands.Table(t.id, t.name, t.participants))))
    }

  def applyEvent(evt: TablesEvent): TablesState =
    evt match {
      case TableAdded(id, name, participants) =>
        this.copy(tables = this.tables :+ TablesPersistence.Table(id, name, participants))
      case TableUpdated(id, name, participants) =>
        val filteredTables = this.tables.filter(_.id != id)
        this.copy(tables = filteredTables :+ TablesPersistence.Table(id, name, participants))
      case TableRemoved(id) =>
        val filteredTables = this.tables.filter(_.id != id)
        this.copy(tables = filteredTables)
    }

}
