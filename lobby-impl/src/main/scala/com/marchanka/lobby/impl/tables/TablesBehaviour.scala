package com.marchanka.lobby.impl.tables

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import com.marchanka.lobby.impl.tables.TablesCommands.TablesCommand
import com.marchanka.lobby.impl.tables.TablesPersistence.{TablesEvent, TablesState}

object TablesBehaviour {

  def create(entityContext: EntityContext[TablesCommand]): Behavior[TablesCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

    create(persistenceId)
      .withTagger(
        AkkaTaggerAdapter.fromLagom(entityContext, TablesEvent.Tag)
      )

  }

  private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
    .withEnforcedReplies[TablesCommand, TablesEvent, TablesState](
      persistenceId = persistenceId,
      emptyState = TablesState.initial,
      commandHandler = (cart, cmd) => cart.applyCommand(cmd),
      eventHandler = (cart, evt) => cart.applyEvent(evt)
    )

}
