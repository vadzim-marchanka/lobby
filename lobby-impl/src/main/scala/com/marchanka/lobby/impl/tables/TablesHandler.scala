package com.marchanka.lobby.impl.tables

import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.marchanka.lobby.impl.tables.TablesCommands._
import com.marchanka.lobby.impl.tables.TablesPersistence._

trait TablesHandler {
  this: TablesState =>

  def applyCommand(cmd: TablesCommand): ReplyEffect[TablesEvent, TablesState] =
    cmd match {
      case AddTable(afterId, id, name, participants, actorRef) =>
        (this.tables.find(_.id == id), this.tables.find(_.id == afterId)) match {
          case (Some(_), _) => Effect.reply(actorRef)(FailedOperationWithBadRequest("The table with given ID exist"))
          case (None, None) if afterId != -1 => Effect.reply(actorRef)(FailedOperationWithBadRequest("There table with afterId does not exist"))
          case _ => Effect.persist(TableAdded(afterId, id, name, participants)).thenReply(actorRef)(_ => SuccessfulOperation)
        }
      case UpdateTable(id, name, participants, actorRef) =>
        this.tables.find(_.id == id) match {
          case None => Effect.reply(actorRef)(FailedOperationWithNotFound)
          case _ => Effect.persist(TableUpdated(id, name, participants)).thenReply(actorRef)(_ => SuccessfulOperation)
        }
      case RemoveTable(id, actorRef) =>
        this.tables.find(_.id == id) match {
          case None => Effect.reply(actorRef)(FailedOperationWithNotFound)
          case _ => Effect.persist(TableRemoved(id)).thenReply(actorRef)(_ => SuccessfulOperation)
        }
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
