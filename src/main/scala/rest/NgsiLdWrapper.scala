package rest

import org.scalatra._
import fiware._
import javax.servlet.ServletConfig
import json._
import main.Configuration
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
class NgsiLdWrapper extends ScalatraServlet with Configuration with WrapperUtils {
  private val Base = System.getenv.getOrDefault(NgsiLdApiPath, DefaultNgsiLdApiPath)

  private val JsonMimeType = "application/json"
  private val JsonLdMimeType = "application/ld+json"

  private val Version = "0.1"

  override def init(servletConfig: ServletConfig) = {
    super.init(servletConfig)
    Console.println("NGSI Endpoint", getServletContext.initParameters(NgsiEndpoint))
    NgsiClient.endpoint(getServletContext.initParameters(NgsiEndpoint))
  }

  def tenant(): Option[String] = {
    request.header("Fiware-Service")
  }


  before() {
    val requestContentType = request.header("Content-Type")
    val requestAccept = request.header("Accept")

    if (!requestContentType.isEmpty && requestContentType.get != JsonMimeType && requestContentType.get != JsonLdMimeType) {
      halt(415)
    }
    else  if (!requestAccept.isEmpty) {
      val mimeTypes = parseAccept(requestAccept.get)
      if(!mimeTypes.contains("*/*") && !mimeTypes.contains(JsonMimeType) && ! mimeTypes.contains(JsonLdMimeType)) {
        halt(415)
      }
    }
  }

  after() {
    val requestContentType = request.header("Content-Type")
    val requestAccept = request.header("Accept")

    // By default MIME type application/json
    contentType = JsonMimeType

    // Accept header has the priority
    if (!requestAccept.isEmpty) {
      val mimeTypes = parseAccept(requestAccept.get)
      if (mimeTypes.contains(JsonLdMimeType)) {
        contentType = JsonLdMimeType
      }
      else if (mimeTypes.contains(JsonMimeType)) {
        contentType = JsonMimeType
      }
    }
    else {
      if (!requestContentType.isEmpty) {
        contentType = requestContentType.get
      }
    }
  }

  get(s"${Base}/") {
    val out = Map(
      "entities_url" -> s"${Base}/entities/",
      "subscriptions_url" -> s"${Base}/subscriptions/",
      "csourceRegistrations_url" -> s"${Base}/csourceRegistrations/"
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
      case _ :Throwable => BadRequest(LdErrors.BadRequestData())
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

        var ldData = toNgsiLd(params,entityData, Ngsi2LdModelMapper.calculateLdContext(entityData))
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
    var queryString = request.getQueryString

   queryString = rewriteQueryString(params,queryString)

    val result = NgsiClient.queryEntities(queryString, tenant())
    result.code match {
      case 200 => {
        val data = result.data.asInstanceOf[List[Map[String, Any]]]
        val out = mutable.ArrayBuffer[Map[String, Any]]()
        for (item <- data) {
          // TODO: the future this should be more sophisticated such as resolving a remote @context
          // Or combine local defined terms with remotely defined terms
          out += toNgsiLd(params,item, Ngsi2LdModelMapper.calculateLdContext(item))
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
    var queryString = request.getQueryString

    val attrs = params.getOrElse("attrs",null)
    if (attrs != null) {
      // Hack to allow getting the @context
      queryString += s"&attrs=${attrs},@context"
    }

    val result = NgsiClient.entityById(id, queryString, tenant())

    result.code match {
      case 200 => {
        val ngsiData = result.data.asInstanceOf[Map[String, Any]]
        val ldData = toNgsiLd(params,ngsiData, Ngsi2LdModelMapper.calculateLdContext(ngsiData))

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
