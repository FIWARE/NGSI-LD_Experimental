package rest

import java.net.{URLDecoder, URLEncoder}

import fiware.Ngsi2LdModelMapper
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.scalatra.Params
import utils.{ExpressionFilterParser, LdContextResolver, ParserUtil}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  * Utilities for the NGSI-LD Wrapper
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
trait WrapperUtils {
  def KeyValues = "keyValues"

  def DefaultContextLink =
    """<https://fiware.github.io/NGSI-LD_Tests/ldContext/defaultContext.jsonld>;
                             rel="http://www.w3.org/ns/json-ld#context";
                             type="application/ld+json";"""

  val linkUri = raw"<(.+)>".r

  val relContext = """rel="(.+)"""".r

  def toNgsiLd(params: Params, in: Map[String, Any], ldContext: Map[String, String]) = {
    if (mode(params) == KeyValues) Ngsi2LdModelMapper.fromNgsiKeyValues(in, ldContext)
    else Ngsi2LdModelMapper.fromNgsi(in, ldContext)
  }

  def mode(params: Params) = {
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

  def parseAccept(accept: String): Array[String] = {
    val mimeTypes = accept.split(',')

    mimeTypes.map((t) => t.split(';')(0))
  }

  /* Obtains the LD Context of an Entity encoded in NGSI-LD */
  def ldContext(data: Map[String, Any]): Map[String, String] = {
    val ldContext = data.getOrElse("@context", Map[String, String]())

    resolveContext(ldContext)
  }

  def resolveContext(ldContextValue: Any): Map[String, String] = {
    val ldContextResolved = mutable.Map[String, String]()

    if (ldContextValue.isInstanceOf[String]) {
      LdContextResolver.resolveContext(ldContextValue.asInstanceOf[String], ldContextResolved)
    }
    else if (ldContextValue.isInstanceOf[List[Any]]) {
      val ldContextLocs = ldContextValue.asInstanceOf[List[Any]]
      ldContextLocs.foreach(l => {
        if (l.isInstanceOf[String]) {
          LdContextResolver.resolveContext(l.asInstanceOf[String], ldContextResolved)
        }
        else if (l.isInstanceOf[Map[String, String]]) {
          ldContextResolved ++= l.asInstanceOf[Map[String, String]]
        }
      })
    }
    else if (ldContextValue.isInstanceOf[Map[String, String]]) {
      ldContextResolved ++= ldContextValue.asInstanceOf[Map[String, String]]
    }

    ldContextResolved.toMap[String, String]
  }

  def partialAttrCheck(payloadData: Map[String, Any], ngsiData: Any, attribute: String): ValidationResult = {
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

  def rewriteFilter(qFilter:String, ldContext: Map[String, String]):String = {
    val qFilterDec = URLDecoder.decode(qFilter)

    val parser = new ExpressionFilterParser(ldContext)

    parser.parse(parser.fullExpr, qFilterDec) match {
      case parser.Success(matched,_) => matched.asInstanceOf[String]
      case parser.Failure(msg,_) => throw new Exception(s"Unexpected failure: ${msg}")
      case parser.Error(msg,_) => throw new Exception(s"Unexpected error: ${msg}")
    }
  }

  def rewriteQueryString(params: Params, in: String, ldContext: Map[String, String]): String = {
    var out = in

    // If there is no ldContext then no need to rewrite attributes or types
    if (ldContext.size > 0) {

      val originalAttrs = params.getOrElse("attrs", None)
      if (originalAttrs != None) {
        // Rewrite attributes as per the @context
        val attrList = originalAttrs.asInstanceOf[String].split(",");

        var finalAttrList = ListBuffer[String]()
        attrList.foreach(attr => {
          finalAttrList += URLEncoder.encode((ldContext.getOrElse(attr, attr)))
        })

        // Hack to allow getting the @context
        finalAttrList += ("@context")
        val newAttrs = finalAttrList.mkString(",")
        val attrs = URLEncoder.encode(newAttrs)

        out += s"&attrs=${attrs}"
      }

      val originalType = params.getOrElse("type", None)
      if (originalType != None) {
        val types = originalType.asInstanceOf[String].split(",")
        var finalTypes = ListBuffer[String]()
        types.foreach(aType => {
          finalTypes += URLEncoder.encode((ldContext.getOrElse(aType, aType)))
        })

        val newTypes = finalTypes.mkString(",")
        val typeStr = URLEncoder.encode(newTypes)

        out += s"&type=${typeStr}"
      }

      val originalQ = params.getOrElse("q", None)

      if(originalQ != None) {
        val newQ = rewriteFilter(originalQ.asInstanceOf[String],ldContext)

        out += s"&q=${URLEncoder.encode(newQ)}"
      }
    }
    
    var geoRel = params.getOrElse("georel", None)
    if (geoRel != None) {
      geoRel = geoRel.asInstanceOf[String].replace("==", ":")
    }

    val coords = params.getOrElse("coordinates", None)
    val geometry = params.getOrElse("geometry", None)

    if (coords != None && geometry != None) {
      out += s"&geometry=point&georel=${geoRel}"

      // Transformation to GeoJson object
      val parsedCoordinates = ParserUtil.parse(coords.asInstanceOf[String])

      // For the moment only GeoJSON objects of type "Point" are supported
      val coordsArray = parsedCoordinates.asInstanceOf[List[Any]]
      geometry match {
        case "Point" => out += s"&coords=${coordsArray(1)},${coordsArray(0)}"
        // TODO: Support other GeoJSON geometries
        case _ => {}
      }
    }

    out
  }

  // Obtains the LD @context supplied in the request
  def requestLdContext(linkHeader: String): Map[String, String] = {
    val out = mutable.Map[String, String]()

    if (linkHeader != null && linkHeader.trim.length > 0) {
      val linkHeaderElements = linkHeader.split(",")

      linkHeaderElements.foreach(link => {
        val linkElements = link.split(";")
        if (linkElements.size >= 3) {
          linkElements(1).trim match {
            case relContext(rel) => (rel) {
              if (rel == "http://www.w3.org/ns/json-ld#context") {
                val ldContextUri = linkElements(0).trim
                ldContextUri match {
                  case linkUri(uri) => (uri) {
                    Console.println("@Context Request", uri)
                    LdContextResolver.resolveContext(uri, out)
                    0
                  }
                }
              }
              0
            }
          }
        }
      })
    }

    out.toMap[String, String]
  }
}