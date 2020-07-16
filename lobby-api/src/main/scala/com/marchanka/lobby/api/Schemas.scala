package com.marchanka.lobby.api

import play.api.libs.json.{Format, Json}

object Schemas {

  case class AddTable(id: Int, name: String, participants: Int)

  object AddTable {
    implicit val format: Format[AddTable] = Json.format[AddTable]
  }

  case class UpdateTable(name: String, participants: Int)

  object UpdateTable {
    implicit val format: Format[UpdateTable] = Json.format[UpdateTable]
  }

  case class Table(id: Int, name: String, participants: Int)

  object Table {
    implicit val format: Format[Table] = Json.format[Table]
  }

}
