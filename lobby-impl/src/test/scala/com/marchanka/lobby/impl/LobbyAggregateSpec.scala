package com.marchanka.lobby.impl

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import com.marchanka.lobby.impl.ActorCommands.{Accepted, Confirmation, Greeting, Hello, UseGreetingMessage}
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

class LobbyAggregateSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with WordSpecLike with Matchers {

  "lobby aggregate" should {

    "say hello by default" in {
      val probe = createTestProbe[Greeting]()
      val ref = spawn(ActorBehaviour.create(PersistenceId("fake-type-hint", "fake-id")))
      ref ! Hello("Alice", probe.ref)
      probe.expectMessage(Greeting("Hello, Alice!"))
    }

    "allow updating the greeting message" in  {
      val ref = spawn(ActorBehaviour.create(PersistenceId("fake-type-hint", "fake-id")))

      val probe1 = createTestProbe[Confirmation]()
      ref ! UseGreetingMessage("Hi", probe1.ref)
      probe1.expectMessage(Accepted)

      val probe2 = createTestProbe[Greeting]()
      ref ! Hello("Alice", probe2.ref)
      probe2.expectMessage(Greeting("Hi, Alice!"))
    }

  }
}
