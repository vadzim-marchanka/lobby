package com.marchanka.lobby.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{EmptyJsonSerializerRegistry, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.marchanka.lobby.api.LobbyService
import com.marchanka.lobby.impl.tables.TablesBehaviour
import com.marchanka.lobby.impl.tables.TablesPersistence.TablesState
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class LobbyLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LobbyApplication(context) with ConfigurationServiceLocatorComponents

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
    Entity(TablesState.typeKey)(
      entityContext => TablesBehaviour.create(entityContext)
    )
  )
}
