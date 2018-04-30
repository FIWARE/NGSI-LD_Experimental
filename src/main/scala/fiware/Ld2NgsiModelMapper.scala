package fiware

import scala.collection.mutable

object Ld2NgsiModelMapper extends Mapper {

  def parse_urn(urn:String) = {
    urn match {
      case UrnPattern(t,id) => (id,t)
    }
  }

  def toNgsi(in: Map[String, Any]) = {
    val out = mutable.Map[String, Any]()

    out += ("@context" -> "http://example.org/jsonld/ngsi-ld.json")

    in.keys.foreach(key => key match {
      case "id" => out += (key -> in(key))
      case "type" => out += (key -> in(key))
      case _ => match_key_ngsi(key, in, out)
    })

    out.toMap[String, Any]
  }

  private def match_key_ngsi(key: String,
                             in: Map[String, Any],
                             out: mutable.Map[String,Any],
                             parentKey:String=null):mutable.Map[String,Any] = {
    val auxIn = in(key).asInstanceOf[Map[String, Any]]

    val attrMap = mutable.Map[String, Any]()
    val metadata = mutable.Map[String,Any]()

    auxIn.keys.foreach((ikey) => ikey match {
      case "type" => {
        val nodeType = auxIn("type")

        nodeType match {
          case "Property" => attrMap += ("value" -> auxIn.getOrElse("value",null))
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
        match_key_ngsi(ikey,auxIn,metadata,key)
      }
    })

    if (metadata.size > 0)
      attrMap += ("metadata" -> metadata.toMap[String,Any])

    out += (key -> attrMap.toMap[String,Any])
  }
}