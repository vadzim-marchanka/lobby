package com.marchanka.lobby.impl.tables

import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.marchanka.lobby.impl.tables.TablesCommands._
import com.marchanka.lobby.impl.tables.TablesPersistence._

trait TablesHandler {
  this: TablesState =>

  def applyCommand(cmd: TablesCommand): ReplyEffect[TablesEvent, TablesState] =
    cmd match {
      case AddTable(afterId, id, name, participants) =>
        Effect.persist(TableAdded(afterId, id, name, participants)).thenNoReply()
      case UpdateTable(id, name, participants) =>
        Effect.persist(TableUpdated(id, name, participants)).thenNoReply()
      case RemoveTable(id) =>
        Effect.persist(TableRemoved(id)).thenNoReply()
      case GetTables(actorRef) =>
        Effect.reply(actorRef)(Tables(this.tables.map(t => TablesCommands.Table(t.id, t.name, t.participants))))
    }

  def applyEvent(evt: TablesEvent): TablesState =
    evt match {
      case TableAdded(afterId, id, name, participants) =>
        val newTable = TablesPersistence.Table(id, name, participants)
        val reorderedTables: Vector[TablesPersistence.Table] = afterId match {
          case -1 => newTable +: this.tables
          case _ =>
            val (front, back) = tables.splitAt(tables.indexWhere(_.id == afterId) + 1)
            (front :+ newTable) ++ back
        }
        this.copy(tables = reorderedTables)
      case TableUpdated(id, name, participants) =>
        val (front, back) = tables.splitAt(tables.indexWhere(_.id == id))
        val updatedTable = TablesPersistence.Table(id, name, participants)
        this.copy(tables = (front :+ updatedTable) ++ back.tail)
      case TableRemoved(id) =>
        val filteredTables = this.tables.filter(_.id != id)
        this.copy(tables = filteredTables)
    }

}
