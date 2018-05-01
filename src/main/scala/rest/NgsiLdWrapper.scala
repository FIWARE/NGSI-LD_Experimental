package rest

import org.scalatra._
import fiware._
import json._
import utils.NgsiClient

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

  // Empty API base
  val base = ""
  val jsonMimeType = Map("Content-Type" -> "application/json")

  get("/version") {
    val versionData = Map("version" -> "0.1")
    Ok(JSONSerializer.serialize(versionData), jsonMimeType)
  }

  get(s"${base}/entities") {
    val t = params.getOrElse("type", null)

    if (t == null) {
      // status(400);
    }

    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData),jsonMimeType)
  }

  // Entity by id
  get(s"${base}/entities/:id") {
    val id = params("id")

    val data = NgsiClient.entityById(id)

    val ldData = Ngsi2LdModelMapper.fromNgsi(data)

    Ok(JSONSerializer.serialize(ldData),jsonMimeType)
  }

  // Subscriptions
  get(s"${base}/subscriptions") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData), jsonMimeType)
  }

  // Csources
  get(s"${base}/csources") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData),jsonMimeType)
  }

  // Delete entity by id
  delete(s"${base}/entities/:id") {

  }

  patch(s"${base}/entities/:id/attrs") {

  }
}
