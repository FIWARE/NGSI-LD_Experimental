package server

import org.scalatra._
import fiware._
import json._

/**
  *
  *  Root class that offers access to the different resources
  *
  *  Coypright (c) 2018 FIWARE Foundation e.V.
  *
  *  Author: José M. Cantera
  *
  *  LICENSE: MIT
  *
  *
  */
class NgsiLdWrapper extends ScalatraServlet {

  val base = "/ngsi-ld/v1"
  val jsonMimeType = Map("Content-Type" -> "application/json")

  get("/version") {
    Ok("0.1", Map("Content-Type" -> "text/plain"))
  }

  get(s"${base}/entities") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData),jsonMimeType)
  }

  get(s"${base}/subscriptions") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData), jsonMimeType)
  }

  get(s"${base}/csources") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData),jsonMimeType)
  }
}
