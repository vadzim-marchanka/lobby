package com.marchanka.lobby.impl

import java.time.LocalDateTime

import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.marchanka.lobby.impl.ActorCommands.{Accepted, ActorCommand, Greeting, Hello, UseGreetingMessage}
import com.marchanka.lobby.impl.ActorPersistence.{ActorEvent, ActorState, GreetingMessageChanged}

trait ActorHandler {
  this: ActorState =>

  def applyCommand(cmd: ActorCommand): ReplyEffect[ActorEvent, ActorState] =
    cmd match {
      case x: Hello              => onHello(x)
      case x: UseGreetingMessage => onGreetingMessageUpgrade(x)
    }

  def applyEvent(evt: ActorEvent): ActorState =
    evt match {
      case GreetingMessageChanged(msg) => updateMessage(msg)
    }
  private def onHello(cmd: Hello): ReplyEffect[ActorEvent, ActorState] =
    Effect.reply(cmd.replyTo)(Greeting(s"$message, ${cmd.name}!"))

  private def onGreetingMessageUpgrade(
                                        cmd: UseGreetingMessage
                                      ): ReplyEffect[ActorEvent, ActorState] =
    Effect
      .persist(GreetingMessageChanged(cmd.message))
      .thenReply(cmd.replyTo) { _ =>
        Accepted
      }

  private def updateMessage(newMessage: String) =
    copy(newMessage, LocalDateTime.now().toString)

}
