package com.marchanka.lobby.impl

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import com.marchanka.lobby.impl.ActorCommands.ActorCommand
import com.marchanka.lobby.impl.ActorPersistence.{ActorEvent, ActorState}

object ActorBehaviour {

  def create(entityContext: EntityContext[ActorCommand]): Behavior[ActorCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

    create(persistenceId)
      .withTagger(
        AkkaTaggerAdapter.fromLagom(entityContext, ActorEvent.Tag)
      )

  }

  private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
    .withEnforcedReplies[ActorCommand, ActorEvent, ActorState](
      persistenceId = persistenceId,
      emptyState = ActorState.initial,
      commandHandler = (cart, cmd) => cart.applyCommand(cmd),
      eventHandler = (cart, evt) => cart.applyEvent(evt)
    )

}
