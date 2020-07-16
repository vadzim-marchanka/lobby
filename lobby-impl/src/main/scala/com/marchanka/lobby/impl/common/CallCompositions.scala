package com.marchanka.lobby.impl.common

import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.ExecutionContext

trait CallCompositions {

  def withAcceptedResponse[Request, Response](
                                               call: ServerServiceCall[Request, Response]
                                             )(implicit ec: ExecutionContext): ServerServiceCall[Request, Response] =
    ServerServiceCall { (_, request) =>
      call
        .invoke(request)
        .map(resp => (ResponseHeader.Ok.withStatus(202), resp))
    }

}
