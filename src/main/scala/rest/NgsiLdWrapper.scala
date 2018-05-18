package rest

import org.scalatra._
import fiware._
import javax.servlet.ServletConfig
import json._
import main.Configuration
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import utils._
import JSONSerializer.serialize

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
class NgsiLdWrapper extends ScalatraServlet with Configuration {

  // Empty API base
  private val Base = "/api"
  private val JsonMimeType = "application/json"
  private val Version = "0.1"

  private val KeyValues = "keyValues"

  override def init(servletConfig: ServletConfig) = {
    super.init(servletConfig)
    Console.println("NGSI Endpoint",getServletContext.initParameters(NgsiEndpoint))
    NgsiClient.endpoint(getServletContext.initParameters(NgsiEndpoint))
  }

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

  def errorDescription(httpEntity: HttpEntity) = {
    val errorPayload = ParserUtil.parse(EntityUtils.toString(
      httpEntity, "UTF-8")).asInstanceOf[Map[String,String]]

    Some(errorPayload("description"))
  }

  def defaultContext = Map("Link" -> DefaultContextLink)

  before() {
    contentType = JsonMimeType
  }

  get(s"${Base}/") {
    val out = Map(
      "entities_url" -> s"${Base}/entities/",
      "subscriptions_url" -> s"${Base}/subscriptions/",
      "csources_url" -> s"${Base}/csources/"
    )

    Ok(serialize(out))
  }

  get("/version") {
    val versionData = Map("version" -> Version,"product" -> "NGSI-LD Wrapper")
    Ok(serialize(versionData))
  }

  get("/configuration") {
    val ngsiEndpoint = getServletContext.initParameters(NgsiEndpoint)
    Ok(serialize(Map("NGSI_Endpoint" -> ngsiEndpoint)))
  }

  post(s"${Base}/entities/") {
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String,Any]]

    val result = NgsiClient.createEntity(Ld2NgsiModelMapper.toNgsi(data))

    result.getStatusLine.getStatusCode match {
      case 201 => Created(null,Map("Location" -> s"${Base}/entities/${data("id")}"))
      case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
      case 422 => {
        Conflict(serialize(LdErrors.AlreadyExists()))
      }
      case _ => InternalServerError()
    }
  }

  post(s"${Base}/entities/:id/attrs/") {
    val id = params("id")
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String,Any]]

    val result = NgsiClient.appendAttributes(id,Ld2NgsiModelMapper.toNgsi(data))

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError()
    }
  }

  patch(s"${Base}/entities/:id/attrs/") {
    /*
    val id = params("id")
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String,Any]]

    val result = NgsiClient.appendAttributes(id,Ld2NgsiModelMapper.toNgsi(data))

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError()
    }*/
  }

  // Delete entity attribute
  delete(s"${Base}/entities/:id/attrs/:attrId") {
    val result = NgsiClient.deleteEntityAttribute(params("id"), params("attrId"))

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError
    }
  }

  get(s"${Base}/entities/") {
    val t = params.getOrElse("type", None)

    if (t == None) {
      BadRequest(serialize(LdErrors.BadRequestData(Some("Entity type not provided"))))
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
          Ok(serialize(out.toList),defaultContext)
        }
        case 400 => {
          BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.httpEntity))))
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

        Ok(serialize(ldData))
      }
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError()
    }
  }

  // Subscriptions
  get(s"${Base}/subscriptions/") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(serialize(myData))
  }

  // Csources
  get(s"${Base}/csources/") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(serialize(myData))
  }
}
