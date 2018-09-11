package rest

import fiware.Ngsi2LdModelMapper
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.scalatra.Params
import utils.ParserUtil

trait WrapperUtils {
  def KeyValues = "keyValues"
  def DefaultContextLink = """<http://uri.etsi.org/ngsi-ld/context.jsonld>;
                             rel="http://www.w3.org/ns/json-ld#context";
                             type="application/ld+json";"""


  def toNgsiLd(params:Params, in: Map[String, Any], ldContext: Map[String, String]) = {
    if (mode(params) == KeyValues) in
    else Ngsi2LdModelMapper.fromNgsi(in, ldContext)
  }

  def mode(params:Params) = {
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

  def parseAccept(accept:String): Array[String] = {
    val mimeTypes = accept.split(',')

    mimeTypes.map((t) => t.split(';')(0))
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
}
