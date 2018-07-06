package fiware

import java.net.URLEncoder

import scala.collection.mutable

/**
  *
  *  Data Mapper from NGSI-LD to NGSI information models
  *
  *  Coypright (c) 2018 FIWARE Foundation e.V.
  *
  *  Author: José M. Cantera
  *
  *  LICENSE: MIT
  *
  *
  */
object Ld2NgsiModelMapper extends Mapper {

  def parse_urn(urn:String) = {
    urn match {
      case UrnPattern(t,id) => (id,t)
    }
  }

  def ldContextualize(ldContext:Map[String,String],term:String) = {
    URLEncoder.encode(ldContext.getOrElse(term,term))
  }

  def toNgsi(in: Map[String, Any],ldContext:Map[String,String]) = {
    val out = mutable.Map[String, Any]()

    in.keys.foreach(key => key match {
      case "id" => out += (key -> in(key))
      case "type" => out += (key -> ldContextualize(ldContext,in(key).asInstanceOf[String]))
      case "@context" => {
        out += (key -> Map("type" -> "@context","value" -> in(key)))
      }
      case _ => match_key_ngsi(key, in, out,null,ldContext)
    })

    out.toMap[String, Any]
  }

  private def match_key_ngsi(key: String,
                             in: Map[String, Any],
                             out: mutable.Map[String,Any],
                             parentKey:String=null,
                             ldContext:Map[String,String]):mutable.Map[String,Any] = {
    val auxIn = in(key).asInstanceOf[Map[String, Any]]

    val attrMap = mutable.Map[String, Any]()
    val metadata = mutable.Map[String,Any]()

    auxIn.keys.foreach((ikey) => ikey match {
      case "type" => {
        val nodeType = auxIn("type")

        nodeType match {
          case "Property" => {
            attrMap += ("value" -> auxIn.getOrElse("value",null))
          }
          case "Relationship" => {
            val urnObject = auxIn("object")
            attrMap += ("type" -> "Relationship", "value" -> urnObject)
            if (parentKey == null)
              metadata += ("entityType" -> Map("value" -> parse_urn(urnObject.asInstanceOf[String])._2))
          }
          case "GeoProperty" => attrMap += ("type" -> "geo:json", "value" -> auxIn("value"))
          case "TemporalProperty" => attrMap += ("type" -> "DateTime","value" -> auxIn("value"))
          case _ => throw new Exception("Node type not provided")
        }
      }
      case "observedAt" => metadata += ("timestamp" -> Map("value" -> auxIn(ikey), "type" -> "DateTime"))
      case "unitCode" => metadata += ("unitCode" -> Map("value" -> auxIn(ikey)))
      case "value" => Nil
      case "object" => Nil
      case _ => {
        match_key_ngsi(ikey,auxIn,metadata,key,ldContext)
      }
    })

    if (metadata.size > 0)
      attrMap += ("metadata" -> metadata.toMap[String,Any])

    out += (ldContextualize(ldContext,key) -> attrMap.toMap[String,Any])
  }
}