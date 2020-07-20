package com.marchanka.lobby.impl.tables

import akka.actor.typed.ActorRef
import com.marchanka.lobby.impl.common.JacksonSerializable

object TablesCommands {

  sealed trait TablesCommand extends JacksonSerializable

  sealed trait TablesReply extends JacksonSerializable

  case class Table(id: Int, name: String, participants: Int)

  case class AddTable(afterId: Int, id: Int, name: String, participants: Int,
                      actorRef: ActorRef[OperationResult]) extends TablesCommand

  case class UpdateTable(id: Int, name: String,
                         participants: Int, actorRef: ActorRef[OperationResult]) extends TablesCommand

  case class RemoveTable(id: Int, actorRef: ActorRef[OperationResult]) extends TablesCommand

  sealed trait OperationResult extends TablesReply

  case object SuccessfulOperation extends OperationResult

  case class FailedOperationWithBadRequest(validationMessage: String) extends OperationResult

  case object FailedOperationWithNotFound extends OperationResult

  case class GetTables(reply: ActorRef[Tables]) extends TablesCommand

  case class Tables(tables: Vector[Table]) extends TablesReply

}
