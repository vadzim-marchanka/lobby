package com.marchanka.lobby.impl.tables

import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.marchanka.lobby.impl.common.JacksonSerializable
import com.marchanka.lobby.impl.tables.TablesCommands.TablesCommand

object TablesPersistence {

  sealed trait TablesEvent extends AggregateEvent[TablesEvent] with JacksonSerializable {
    def aggregateTag: AggregateEventTag[TablesEvent] = TablesEvent.Tag
  }

  object TablesEvent {
    val Tag: AggregateEventTag[TablesEvent] = AggregateEventTag[TablesEvent]
  }

  case class TableAdded(id: Int, name: String, participants: Int) extends TablesEvent

  case class TableUpdated(id: Int, name: String, participants: Int) extends TablesEvent

  case class TableRemoved(id: Int) extends TablesEvent

  case class Table(id: Int, name: String, participants: Int)

  case class TablesState(tables: Vector[Table]) extends JacksonSerializable with TablesHandler

  object TablesState {
    def initial: TablesState = TablesState(Vector.empty)

    val typeKey = EntityTypeKey[TablesCommand]("TablesAggregate")
  }

}
