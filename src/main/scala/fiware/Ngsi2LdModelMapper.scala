package fiware

import java.net.URLDecoder

import rest.WrapperUtils
import utils.LdContextResolver

import scala.collection.mutable

/**
  *
  * Data Mapper from NGSI to NGSI-LD information models
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
object Ngsi2LdModelMapper extends Mapper with WrapperUtils {

  def fromNgsi(in: Map[String, Any],ldContext: Map[String, String]) = {
    val out = mutable.Map[String, Any]()

    val revContext = ldReverseContext(ldContext)

    in.keys.foreach(key => key match {
      case "id" => out += (key -> format_uri(in(key).asInstanceOf[String],
        in("type").asInstanceOf[String]))
      case "type" => out += (key -> reversed(in(key).asInstanceOf[String],revContext))
      case _ => match_key(key, in, out, revContext)
    })

    out.toMap[String, Any]
  }

  def calculateLdContext(entity: Map[String, Any]):Map[String,String] = {
    if (!entity.contains("@context")) {
      return Map[String,String]()
    }

    val ldContextValue = entity("@context").asInstanceOf[Map[String,Any]].getOrElse("value",None)

    if (ldContextValue == None) {
      Map[String,String]()
    }
    else {
      resolveContext(ldContextValue)
    }
  }

  private def ldReverseContext(ldContext:Map[String,String]): Map[String, String] = {
    val reverseContext = mutable.Map[String, String]()

    ldContext.keys.foreach(key => {
      reverseContext += (ldContext(key) -> key)
    })

    reverseContext.toMap[String, String]
  }

  private def reversed(p: String, reverseContext: Map[String, String]) = {
    reverseContext.getOrElse(URLDecoder.decode(p), URLDecoder.decode(p))
  }

  private def match_key(key: String, in: Map[String, Any], out: mutable.Map[String, Any],
                        ldReversedContext: Map[String, String]): Any = {
    val auxIn = in(key).asInstanceOf[Map[String, Any]]

    key match {
      case "dateCreated" => out += ("createdAt" -> auxIn("value"))
      case "dateModified" => out += ("modifiedAt" -> auxIn("value"))
      case "@context" => out += ("@context" -> auxIn("value"))

      case AnyProp(prop) => {
        val nodeType = ((p: String) => {
          var out: (String, String, String) = null

          p match {
            case ReferenceAttr(relName) => out = rel_member(p)
            case _ => out = (reversed(p, ldReversedContext), "Property", "value")
          }
          val declType = auxIn.getOrElse("type", "Property")
          declType match {
            case "Relationship" => out = rel_member(reversed(p, ldReversedContext))
            case "Reference" => out = rel_member(reversed(p, ldReversedContext))
            case "geo:json" => out = (reversed(p, ldReversedContext), "GeoProperty", "value")
            case "DateTime" => out = (reversed(p, ldReversedContext), "TemporalProperty", "value")
            case _ => Nil
          }
          out
        }) (prop)

        val propMap = mutable.Map[String, Any]("type" -> nodeType._2)

        auxIn.keys.foreach(key => key match {
          case "value" => propMap += (nodeType._3 -> format_value(nodeType._2, auxIn("value")))
          case "metadata" => {
            val auxMeta = auxIn("metadata").asInstanceOf[Map[String, Any]]
            auxMeta.keys.foreach(metaKey => {
              val auxMetaProp = auxMeta(metaKey).asInstanceOf[Map[String, Any]]
              metaKey match {
                case "timestamp" => {
                  // Removing timezone and trailing fractional seconds (in the ETSI spec the ',' should be the separator)
                  propMap += ("observedAt" -> auxMetaProp("value").asInstanceOf[String].dropRight(1).split('.')(0))
                }
                case "unitCode" => propMap += ("unitCode" -> auxMetaProp("value"))
                case "entityType" => {
                  val entityId = propMap.getOrElse("object", null).asInstanceOf[String]
                  if (entityId != null) {
                    propMap("object") = format_uri(entityId, auxMetaProp("value").asInstanceOf[String])
                  }
                }
                case _ => match_key(metaKey, auxMeta, propMap, ldReversedContext)
              }
            })
          }
          case "type" => {}
          case _ => Nil
        })

        out += (nodeType._1 -> propMap.toMap[String, Any])
      }
      case _ => Nil
    }
  }

  private def rel_member(attrName: String) = {
    (attrName, "Relationship", "object")
  }

  private def format_value(nodeType: String, value: Any, entityType: String = null) = {
    var out: Any = value

    if (nodeType == "Relationship") {
      out = format_uri(value.asInstanceOf[String], entityType)
    }

    out
  }

  private def format_uri(id: String, entityType: String) = {
    val eType = if (entityType == null) "Thing" else entityType

    id match {
      case UrnPattern(entType, entId) => if (entType == "Thing") toURN(entId, eType) else id
      case _ => toURN(id, eType)
    }
  }

  def toURN(id: String, entityType: String) = s"urn:ngsi-ld:${entityType}:${id}"

}