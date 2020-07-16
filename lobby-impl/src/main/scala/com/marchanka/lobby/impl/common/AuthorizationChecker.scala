package com.marchanka.lobby.impl.common

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, ResponseHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.marchanka.lobby.impl.common.AuthorizationChecker.{AdminRole, UserRole}
import play.mvc.Http.HeaderNames

import scala.concurrent.ExecutionContext

trait AuthorizationChecker {

  def withRoleAuthorizationChecking[Request, Response](requiredRoles: String*)(
                                                       call: ServerServiceCall[Request, Response]
                                                       )(implicit ec: ExecutionContext): ServerServiceCall[Request, Response] =
    ServerServiceCall { (header, request) =>
      header.getHeader(HeaderNames.AUTHORIZATION) match {
        case Some(value) => throwExceptionIfInvalidToken(requiredRoles)(value)
        case None => throw Forbidden("The authorization header is not specified")
      }
      call.invokeWithHeaders(header, request)
    }

  private def throwExceptionIfInvalidToken(requiredRoles: Seq[String])(token: String) = {
    val bytesWithUserAndPassword = Base64.getDecoder.decode(token.replaceFirst(AuthorizationChecker.BasicAuthenticationPrefix, ""))
    val userAndPasswords = new String(bytesWithUserAndPassword, StandardCharsets.UTF_8).split(":")
    userAndPasswords match {
      case Array(user, password) if user == AdminRole && password == AdminRole && requiredRoles.contains(AdminRole) => ()
      case Array(user, password) if user == UserRole && password == UserRole && requiredRoles.contains(UserRole) => ()
      case _ => throw Forbidden("The authorization header does not correspond to the role")
    }
  }

}

object AuthorizationChecker {

  val BasicAuthenticationPrefix = "Basic "
  val AdminRole = "admin"
  val UserRole = "user"

}
