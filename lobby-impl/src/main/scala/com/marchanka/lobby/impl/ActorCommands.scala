package com.marchanka.lobby.impl

import akka.actor.typed.ActorRef
import com.marchanka.lobby.impl.common.JacksonSerializable

object ActorCommands {

  sealed trait ActorCommand extends JacksonSerializable
  sealed trait ActorReply extends JacksonSerializable

  case class UseGreetingMessage(message: String, replyTo: ActorRef[Confirmation]) extends ActorCommand
  case class Hello(name: String, replyTo: ActorRef[Greeting]) extends ActorCommand

  final case class Greeting(message: String) extends ActorReply
  trait Confirmation extends ActorReply
  case object Rejected extends Confirmation
  case object Accepted extends Confirmation

}
