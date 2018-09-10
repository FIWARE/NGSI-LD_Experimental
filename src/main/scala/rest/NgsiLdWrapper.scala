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
  * Root class that offers access to the different resources
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
class NgsiLdWrapper extends ScalatraServlet with Configuration {
  private val Base = System.getenv.getOrDefault(NgsiLdApiPath, DefaultNgsiLdApiPath)

  private val JsonMimeType = "application/json"
  private val Version = "0.1"

  private val KeyValues = "keyValues"

  override def init(servletConfig: ServletConfig) = {
    super.init(servletConfig)
    Console.println("NGSI Endpoint", getServletContext.initParameters(NgsiEndpoint))
    NgsiClient.endpoint(getServletContext.initParameters(NgsiEndpoint))
  }

  def tenant(): Option[String] = {
    request.header("Fiware-Service")
  }

  def toNgsiLd(in: Map[String, Any], ldContext: Map[String, String]) = {
    if (mode == KeyValues) in
    else Ngsi2LdModelMapper.fromNgsi(in, ldContext)
  }

  def mode() = {
    var options = params.getOrElse("options", null)
    if (options != null)
      if (options.split(",").indexOf(KeyValues) != -1)
        options = KeyValues

    options
  }

  def errorDescription(httpEntity: HttpEntity) = {
    val errorPayload = ParserUtil.parse(EntityUtils.toString(
      httpEntity, "UTF-8")).asInstanceOf[Map[String, String]]

    Some(errorPayload("description"))
  }

  def defaultContext = Map("Link" -> DefaultContextLink)

  before() {
    contentType = JsonMimeType
  }

  def ldContext(data: Map[String, Any]): Map[String, String] = {
    val ldContext = data.getOrElse("@context", Map[String, String]())

    // For the time being the LD @context is not resolved externally and only JSON objects are allowed
    if (ldContext.isInstanceOf[Map[String, String]])
      ldContext.asInstanceOf[Map[String, String]]
    else Map[String, String]()
  }

  def partialAttrCheck(payloadData:Map[String,Any],ngsiData:Any,attribute:String):ValidationResult = {
    if (payloadData.isEmpty)
      EmptyPayload()
    else {
      // If the Entity does not exist or currently does not have such an Attribute 404 is returned
      if (ngsiData == None)
        EntityNotFound()
      else {
        val entityData = ngsiData.asInstanceOf[Map[String, Any]]
        // TODO: This might need to be checked taking into account a @context
        if (!entityData.contains(attribute))
          AttributeNotFound()
        else
          ValidInput()
      }
    }
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
    val versionData = Map("version" -> Version, "product" -> "NGSI-LD Wrapper")
    Ok(serialize(versionData))
  }

  get("/configuration") {
    val ngsiEndpoint = getServletContext.initParameters(NgsiEndpoint)
    Ok(serialize(Map("NGSI_Endpoint" -> ngsiEndpoint)))
  }

  post(s"${Base}/entities/") {
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String, Any]]

    try {
      val ngsiData = Ld2NgsiModelMapper.toNgsi(data, ldContext(data))


      val result = NgsiClient.createEntity(ngsiData, tenant())

      result.getStatusLine.getStatusCode match {
        case 201 => Created(null, Map("Location" -> s"${Base}/entities/${data("id")}"))
        case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
        case 422 => {
          Conflict(serialize(LdErrors.AlreadyExists()))
        }
        case _ => InternalServerError()
      }
    } catch {
      case _ => BadRequest(LdErrors.BadRequestData())
    }
  }

  post(s"${Base}/entities/:id/attrs/") {
    val id = params("id")
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String, Any]]

    val result = NgsiClient.appendAttributes(id, Ld2NgsiModelMapper.toNgsi(data, ldContext(data)), tenant())

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError()
    }
  }

  patch(s"${Base}/entities/:id/attrs/") {
    val id = params("id")
    val data = ParserUtil.parse(request.body).asInstanceOf[Map[String, Any]]

    val result = NgsiClient.updateEntity(id, Ld2NgsiModelMapper.toNgsi(data, ldContext(data)), tenant())

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError()
    }
  }

  patch(s"${Base}/entities/:id/attrs/:attrId") {
    val id = params("id")
    val attribute = params("attrId")
    // Attribute Data
    val attrData = ParserUtil.parse(request.body).asInstanceOf[Map[String, Any]]

    // First Entity Content is queried
    val ngsiData = NgsiClient.entityById(id, null, tenant()).data

    val valResult = partialAttrCheck(attrData,ngsiData,attribute)

    valResult match {
      case EmptyPayload() =>  BadRequest(serialize(LdErrors.NotFound()))
      case EntityNotFound() => NotFound(serialize(LdErrors.NotFound()))
      case AttributeNotFound() => NotFound(serialize(LdErrors.NotFound()))
      case ValidInput() => {
        val entityData = ngsiData.asInstanceOf[Map[String, Any]]

        var ldData = toNgsiLd(entityData, Ngsi2LdModelMapper.ldContext(entityData))
        // Then Entity data is properly updated with the new values, but not needed stuff is removed
        ldData -= ("id", "type")

        var affectedAttribute = ldData(attribute).asInstanceOf[Map[String, Any]]
        // TODO: check null values so that actually the data item is removed
        attrData.keys.foreach(key => {
          affectedAttribute = affectedAttribute.updated(key,attrData(key))
        })
        ldData = ldData.updated(attribute,affectedAttribute)

        val result = NgsiClient.updateEntity(id, Ld2NgsiModelMapper.toNgsi(ldData, ldContext(ldData)), tenant())

        result.getStatusLine.getStatusCode match {
          case 204 => NoContent()
          case 400 => BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.getEntity))))
          case 404 => NotFound(serialize(LdErrors.NotFound()))
          case _ => InternalServerError()
        }
      }
    }
  }

  delete(s"${Base}/entities/:id") {
    val result = NgsiClient.deleteEntity(params("id"), tenant())

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError
    }
  }

  // Delete entity attribute
  delete(s"${Base}/entities/:id/attrs/:attrId") {
    val result = NgsiClient.deleteEntityAttribute(params("id"), params("attrId"), tenant())

    result.getStatusLine.getStatusCode match {
      case 204 => NoContent()
      case 404 => NotFound(serialize(LdErrors.NotFound()))
      case _ => InternalServerError
    }
  }

  get(s"${Base}/entities/") {
    val queryString = request.getQueryString
    val result = NgsiClient.queryEntities(queryString, tenant())
    result.code match {
      case 200 => {
        val data = result.data.asInstanceOf[List[Map[String, Any]]]
        val out = mutable.ArrayBuffer[Map[String, Any]]()
        for (item <- data) {
          // TODO: the future this should be more sophisticated such as resolving a remote @context
          // Or combine local defined terms with remotely defined terms
          out += toNgsiLd(item, Ngsi2LdModelMapper.ldContext(item))
        }
        Ok(serialize(out.toList), defaultContext)
      }
      case 400 => {
        BadRequest(serialize(LdErrors.BadRequestData(errorDescription(result.httpEntity))))
      }
    }
  }

  // Entity by id
  get(s"${Base}/entities/:id") {
    val id = params("id")
    val queryString = request.getQueryString

    val result = NgsiClient.entityById(id, queryString, tenant())

    result.code match {
      case 200 => {
        val ngsiData = result.data.asInstanceOf[Map[String, Any]]
        val ldData = toNgsiLd(ngsiData, Ngsi2LdModelMapper.ldContext(ngsiData))

        Ok(serialize(ldData), defaultContext)
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

  // Csource Registrations
  get(s"${Base}/csourceRegistrations/") {
    val myData = Map("a" -> 45, "b" -> "hola")
    Ok(serialize(myData))
  }
}
