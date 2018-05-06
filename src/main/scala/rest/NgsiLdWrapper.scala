package rest

import org.scalatra._
import fiware._
import json._
import org.apache.http.util.EntityUtils
import utils._

import scala.collection.mutable


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
  val Base = "/api"
  val JsonMimeType = Map("Content-Type" -> "application/json")
  val Version = "0.1"

  val KeyValues = "keyValues"

  def toNgsiLd(in:Map[String,Any]) = {
    if (mode == KeyValues) in
    else Ngsi2LdModelMapper.fromNgsi(in)
  }

  def mode() = {
    var options = params.getOrElse("options",null)
    if(options != null)
      if (options.split(",").indexOf(KeyValues) != -1)
        options = KeyValues

    options
  }

  get(s"${Base}/") {
    val out = Map(
      "entities" -> s"${Base}/entities/",
      "subscriptions" -> s"${Base}/subscriptions/",
      "csources" -> s"${Base}/csources/"
    )

    Ok(JSONSerializer.serialize(out),JsonMimeType)
  }

  get("/version") {
    val versionData = Map("version" -> Version)
    Ok(JSONSerializer.serialize(versionData), JsonMimeType)
  }

  post(s"${Base}/entities/") {
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String,Any]]

    Console.println(Ld2NgsiModelMapper.toNgsi(data))
    val result = NgsiClient.createEntity(Ld2NgsiModelMapper.toNgsi(data))

    result.getStatusLine.getStatusCode match {
      case 201 => Created(null,Map("Location" -> s"${Base}/entities/${data("id")}"))
      case 400 => BadRequest(EntityUtils.toString(result.getEntity, "UTF-8"),JsonMimeType)
      case _ => InternalServerError()
    }
  }

  get(s"${Base}/entities/") {
    val t = params.getOrElse("type", null)

    if (t == null) {
      BadRequest(JSONSerializer.serialize(Map("type" -> "BadRequest")),JsonMimeType)
    }
    else {
      val queryString = request.getQueryString
      val result = NgsiClient.queryEntities(queryString)
      result.code match {
        case 200 => {
          val data = result.data.asInstanceOf[List[Map[String,Any]]]
          val out = mutable.ArrayBuffer[Map[String,Any]]()
          for (item <- data) {
            out += toNgsiLd(item)
          }
          Ok(JSONSerializer.serialize(out.toList),JsonMimeType)
        }
        case 400 => {
          BadRequest(JSONSerializer.serialize(Map("type" -> "BadRequest")),JsonMimeType)
        }
      }
    }
  }

  // Entity by id
  get(s"${Base}/entities/:id") {
    val id = params("id")

    val queryString = request.getQueryString

    val result = NgsiClient.entityById(id,queryString)

    result.code match {
      case 200 => {
        val ldData = toNgsiLd(result.data.asInstanceOf[Map[String,Any]])

        Ok(JSONSerializer.serialize(ldData),JsonMimeType)
      }
      case 404 => NotFound(JSONSerializer.serialize(Map("type" -> "NotFound")),JsonMimeType)
    }
  }

  // Subscriptions
  get(s"${Base}/subscriptions/") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData), JsonMimeType)
  }

  // Csources
  get(s"${Base}/csources/") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(JSONSerializer.serialize(myData),JsonMimeType)
  }

  // Delete entity by id
  delete(s"${Base}/entities/:id") {
    val result = NgsiClient.deleteEntity(params("id"))

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent(Map.empty)
      case 404 => NotFound(JSONSerializer.serialize(Map("type" -> "NotFound")),JsonMimeType)
      case _ => InternalServerError
    }
  }

  patch(s"${Base}/entities/:id/attrs") {

  }
}
