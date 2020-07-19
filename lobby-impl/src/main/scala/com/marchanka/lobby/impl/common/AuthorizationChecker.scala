package com.marchanka.lobby.impl.common

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.marchanka.lobby.impl.common.AuthorizationChecker.{AdminRole, AuthorizationType, UserRole}
import play.mvc.Http.HeaderNames.AUTHORIZATION

import scala.concurrent.ExecutionContext

trait AuthorizationChecker {

  def withRoleAuthorization[Request, Response](requiredRoles: String*)(
    call: ServerServiceCall[Request, Response]
  )(implicit ec: ExecutionContext): ServerServiceCall[Request, Response] =
    ServerServiceCall { (header, request) =>
      header.getHeader(AUTHORIZATION) match {
        case Some(value) => throwExceptionIfInvalidCredentials(requiredRoles)(value)
        case None => throw Forbidden("The authorization header is not specified")
      }
      call.invokeWithHeaders(header, request)
    }

  private def throwExceptionIfInvalidCredentials(requiredRoles: Seq[String])(headerValue: String) = {
    val credentialsToken = headerValue.replaceFirst(AuthorizationType, "").trim
    val credentials = new String(Base64.getDecoder.decode(credentialsToken), UTF_8).split(":")
    credentials match {
      case Array(user, password) if user == AdminRole && password == AdminRole && requiredRoles.contains(AdminRole) => ()
      case Array(user, password) if user == UserRole && password == UserRole && requiredRoles.contains(UserRole) => ()
      case _ => throw Forbidden("The authorization header does not correspond to the role")
    }
  }

}

object AuthorizationChecker {

  val AuthorizationType = "Basic"
  val AdminRole = "admin"
  val UserRole = "user"

}
