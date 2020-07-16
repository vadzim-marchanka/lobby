package com.marchanka.lobby.impl

import java.time.LocalDateTime

import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.marchanka.lobby.impl.ActorCommands.ActorCommand
import com.marchanka.lobby.impl.common.JacksonSerializable

object ActorPersistence {

  sealed trait ActorEvent extends AggregateEvent[ActorEvent] with JacksonSerializable {
    def aggregateTag: AggregateEventTag[ActorEvent] = ActorEvent.Tag
  }
  object ActorEvent {
    val Tag: AggregateEventTag[ActorEvent] = AggregateEventTag[ActorEvent]
  }

  case class GreetingMessageChanged(message: String) extends ActorEvent

  case class ActorState(message: String, timestamp: String) extends JacksonSerializable with ActorHandler
  object ActorState {
    def initial: ActorState = ActorState("Hello", LocalDateTime.now.toString)

    val typeKey = EntityTypeKey[ActorCommand]("ActorAggregate")
  }

}
