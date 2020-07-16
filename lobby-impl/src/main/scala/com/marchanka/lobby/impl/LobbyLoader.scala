package com.marchanka.lobby.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.marchanka.lobby.api.LobbyService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.{EmptyJsonSerializerRegistry, JsonSerializerRegistry}
import com.marchanka.lobby.impl.ActorPersistence.ActorState
import com.softwaremill.macwire._

class LobbyLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LobbyApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LobbyApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[LobbyService])
}

abstract class LobbyApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  override lazy val lagomServer: LagomServer = serverFor[LobbyService](wire[LobbyServiceImpl])

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = EmptyJsonSerializerRegistry

  clusterSharding.init(
    Entity(ActorState.typeKey)(
      entityContext => ActorBehaviour.create(entityContext)
    )
  )
}
